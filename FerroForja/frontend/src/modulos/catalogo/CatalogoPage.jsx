import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Button,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress,
  Alert,
  Stack,
  Tooltip,
  Autocomplete,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import SearchIcon from '@mui/icons-material/Search';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import clienteApi from '../../servicios/api/clienteApi';
import { formatearMoneda, formatearVolumen } from '../../utilidades/formatos';
import { useSesion } from '../../nucleo/proveedores/SesionProvider';
import { useIdioma } from '../../nucleo/proveedores/IdiomaProvider';

export const CatalogoPage = () => {
  const { hasRole } = useSesion();
  const { t } = useIdioma();
  const isAdmin = hasRole('ROLE_ADMINISTRADOR');

  const [fichas, setFichas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  // Maestros para creación de referencias
  const [bases, setBases] = useState({
    articulos: [],
    fabricantes: [],
    empaques: [],
    medidasBase: [],
  });

  // Modales
  const [openPriceModal, setOpenPriceModal] = useState(false);
  const [selectedRef, setSelectedRef] = useState(null);
  const [openCreateModal, setOpenCreateModal] = useState(false);

  const fetchCatalog = async () => {
    try {
      setLoading(true);
      setError(null);
      const params = new URLSearchParams({ size: 50 });
      if (search) params.append('search', search);

      const response = await clienteApi.get(`/catalogo/fichas?${params.toString()}`);
      setFichas(response.data.content || []);
    } catch (err) {
      setError(t('noItemsFound'));
    } finally {
      setLoading(false);
    }
  };

  const fetchMasters = async () => {
    try {
      const response = await clienteApi.get('/catalogo/bases');
      setBases({
        articulos: response.data.articulos || [],
        fabricantes: response.data.fabricantes || [],
        empaques: response.data.empaques || [],
        medidasBase: response.data.medidasBase || [],
      });
    } catch (err) {
      // Si falla maestros no bloquea catálogo
    }
  };

  useEffect(() => {
    fetchCatalog();
  }, [search]);

  useEffect(() => {
    fetchMasters();
  }, []);

  // Formik para cambio de precio con auditoría (RN-09, RN-11)
  const priceFormik = useFormik({
    initialValues: {
      nuevoTarifa: '',
      causa: '',
    },
    validationSchema: Yup.object({
      nuevoTarifa: Yup.number()
        .positive('El precio debe ser positivo')
        .required('El precio es obligatorio'),
      causa: Yup.string().required('Debe indicar la justificación del cambio para la auditoría'),
    }),
    onSubmit: async (values, { resetForm }) => {
      try {
        await clienteApi.patch(`/catalogo/fichas/${selectedRef.id}/tarifa`, {
          nuevoTarifa: values.nuevoTarifa,
          causa: values.causa,
        });
        setSuccessMsg(`Precio de ${selectedRef.codigoEan} actualizado exitosamente.`);
        setOpenPriceModal(false);
        resetForm();
        fetchCatalog();
      } catch (err) {
        setError(err.response?.data?.detail || 'Error al actualizar el precio');
      }
    },
  });

  // Formik para creación de nueva referencia con Producto + Marca + Presentación seleccionables o libres
  const createFormik = useFormik({
    initialValues: {
      codigoEan: '',
      articuloDenominacion: '',
      fabricanteDenominacion: '',
      empaqueDenominacion: '',
      medidaBaseClave: 'UND',
      tarifaPublico: '',
    },
    validationSchema: Yup.object({
      codigoEan: Yup.string().required('Código de barras obligatorio'),
      articuloDenominacion: Yup.string().required('Debe ingresar o seleccionar el producto base'),
      fabricanteDenominacion: Yup.string().required('Debe ingresar o seleccionar la marca'),
      empaqueDenominacion: Yup.string().required('Debe ingresar o seleccionar la presentación'),
      medidaBaseClave: Yup.string().required('Unidad de medida obligatoria'),
      tarifaPublico: Yup.number().positive('Precio debe ser positivo').required('Precio obligatorio'),
    }),
    onSubmit: async (values, { resetForm }) => {
      try {
        await clienteApi.post('/catalogo/fichas', values);
        setSuccessMsg(`Referencia ${values.codigoEan} creada en el catálogo exitosamente.`);
        setOpenCreateModal(false);
        resetForm();
        fetchCatalog();
        fetchMasters();
      } catch (err) {
        setError(err.response?.data?.detail || 'Error al crear la referencia en el catálogo');
      }
    },
  });

  const handleOpenPrice = (ref) => {
    setSelectedRef(ref);
    priceFormik.setValues({
      nuevoTarifa: ref.tarifaPublico,
      causa: '',
    });
    setOpenPriceModal(true);
  };

  // Activar o desactivar producto (soluciona error de reactivación)
  const handleToggleStatus = async (ref) => {
    const accion = ref.habilitado ? 'deshabilitar' : 'habilitar';
    if (window.confirm(`¿Está seguro de ${accion} la referencia ${ref.codigoEan}?`)) {
      try {
        await clienteApi.patch(`/catalogo/fichas/${ref.id}/conmutar-situacion`);
        setSuccessMsg(`Estado de referencia ${ref.codigoEan} modificado a ${ref.habilitado ? 'DESHABILITADO' : 'HABILITADO'}.`);
        fetchCatalog();
      } catch (err) {
        setError(err.response?.data?.detail || 'Error al cambiar estado de la referencia');
      }
    }
  };

  return (
    <Box>
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h5" fontWeight="bold">
            {t('catalogTitle')}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {t('catalogSubtitle')}
          </Typography>
        </Box>
        {isAdmin && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => {
              createFormik.resetForm();
              setOpenCreateModal(true);
            }}
          >
            {t('newReference')}
          </Button>
        )}
      </Box>

      {successMsg && (
        <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccessMsg(null)}>
          {successMsg}
        </Alert>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      <Paper sx={{ p: 2, mb: 3 }}>
        <TextField
          fullWidth
          size="small"
          placeholder={t('searchPlaceholder')}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          InputProps={{
            startAdornment: <SearchIcon sx={{ color: 'action.active', mr: 1 }} />,
          }}
        />
      </Paper>

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <TableContainer component={Paper} elevation={2}>
          <Table>
            <TableHead sx={{ bgcolor: 'action.hover' }}>
              <TableRow>
                <TableCell><strong>{t('colBarcode')}</strong></TableCell>
                <TableCell><strong>{t('colDescription')}</strong></TableCell>
                <TableCell><strong>{t('colUnit')}</strong></TableCell>
                <TableCell align="right"><strong>{t('colSalePrice')}</strong></TableCell>
                {isAdmin && <TableCell align="right"><strong>{t('colAvgCost')}</strong></TableCell>}
                <TableCell align="center"><strong>{t('colStock')}</strong></TableCell>
                <TableCell align="center"><strong>{t('colStatus')}</strong></TableCell>
                <TableCell align="center"><strong>{t('colActions')}</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {fichas.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} align="center" sx={{ py: 3 }}>
                    {t('noItemsFound')}
                  </TableCell>
                </TableRow>
              ) : (
                fichas.map((ref) => (
                  <TableRow key={ref.id} hover>
                    <TableCell sx={{ fontFamily: 'monospace', fontWeight: 'bold' }}>
                      {ref.codigoEan}
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" fontWeight="medium">
                        {ref.articuloDenominacion}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {ref.fabricanteDenominacion} — {ref.empaqueDenominacion}
                      </Typography>
                    </TableCell>
                    <TableCell>{ref.medidaBaseClave}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold', color: 'text.primary' }}>
                      {formatearMoneda(ref.tarifaPublico)}
                    </TableCell>
                    {isAdmin && (
                      <TableCell align="right" sx={{ color: 'text.secondary' }}>
                        {formatearMoneda(ref.costeMedio, 2)}
                      </TableCell>
                    )}
                    <TableCell align="center">
                      <Chip
                        label={formatearVolumen(ref.existenciaLibre)}
                        color={Number(ref.existenciaLibre) <= 5 ? 'warning' : 'default'}
                        size="small"
                      />
                    </TableCell>
                    <TableCell align="center">
                      <Chip
                        label={ref.habilitado ? t('active') : t('inactive')}
                        color={ref.habilitado ? 'success' : 'default'}
                        size="small"
                      />
                    </TableCell>
                    <TableCell align="center">
                      <Stack direction="row" spacing={1} justifyContent="center">
                        <Tooltip title={t('modifyPrice')}>
                          <IconButton
                            size="small"
                            color="secondary"
                            onClick={() => handleOpenPrice(ref)}
                          >
                            <EditIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        {isAdmin && (
                          <Tooltip title={ref.habilitado ? t('deactivateRef') : t('activateRef')}>
                            <IconButton
                              size="small"
                              color={ref.habilitado ? 'error' : 'success'}
                              onClick={() => handleToggleStatus(ref)}
                            >
                              {ref.habilitado ? <BlockIcon fontSize="small" /> : <CheckCircleOutlineIcon fontSize="small" />}
                            </IconButton>
                          </Tooltip>
                        )}
                      </Stack>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Modal Modificar Precio */}
      <Dialog open={openPriceModal} onClose={() => setOpenPriceModal(false)} maxWidth="xs" fullWidth>
        <DialogTitle>{t('modifyPrice')}</DialogTitle>
        <form onSubmit={priceFormik.handleSubmit}>
          <DialogContent>
            <Typography variant="body2" sx={{ mb: 2 }}>
              Artículo: <strong>{selectedRef?.articuloDenominacion}</strong> ({selectedRef?.codigoEan})
            </Typography>
            <TextField
              fullWidth
              id="nuevoTarifa"
              name="nuevoTarifa"
              label={t('priceLabel')}
              type="number"
              margin="normal"
              value={priceFormik.values.nuevoTarifa}
              onChange={priceFormik.handleChange}
              error={priceFormik.touched.nuevoTarifa && Boolean(priceFormik.errors.nuevoTarifa)}
              helperText={priceFormik.touched.nuevoTarifa && priceFormik.errors.nuevoTarifa}
            />
            <TextField
              fullWidth
              id="causa"
              name="causa"
              label="Motivo o Justificación (Auditoría RN-09/11)"
              margin="normal"
              multiline
              rows={2}
              value={priceFormik.values.causa}
              onChange={priceFormik.handleChange}
              error={priceFormik.touched.causa && Boolean(priceFormik.errors.causa)}
              helperText={priceFormik.touched.causa && priceFormik.errors.causa}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenPriceModal(false)}>{t('cancel')}</Button>
            <Button type="submit" variant="contained">{t('savePrice')}</Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* Modal Crear Referencia Completa (Corrige error de campos faltantes) */}
      <Dialog open={openCreateModal} onClose={() => setOpenCreateModal(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{t('createRefTitle')}</DialogTitle>
        <form onSubmit={createFormik.handleSubmit}>
          <DialogContent>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 2 }}>
              Una referencia comercial única se define combinando Producto + Marca + Presentación + Código de Barras (RN-02). Puede seleccionar existentes o escribir nuevos nombres.
            </Typography>

            <Stack spacing={2}>
              {/* Producto */}
              <Autocomplete
                freeSolo
                options={bases.articulos.map((p) => p.denominacion)}
                value={createFormik.values.articuloDenominacion}
                onChange={(event, newValue) => createFormik.setFieldValue('articuloDenominacion', newValue || '')}
                onInputChange={(event, newInputValue) => createFormik.setFieldValue('articuloDenominacion', newInputValue || '')}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label={`${t('productLabel')} (Ej: Martillo de Uña, Disco de Corte)`}
                    error={createFormik.touched.articuloDenominacion && Boolean(createFormik.errors.articuloDenominacion)}
                    helperText={createFormik.touched.articuloDenominacion && createFormik.errors.articuloDenominacion}
                  />
                )}
              />

              {/* Marca */}
              <Autocomplete
                freeSolo
                options={bases.fabricantes.map((m) => m.denominacion)}
                value={createFormik.values.fabricanteDenominacion}
                onChange={(event, newValue) => createFormik.setFieldValue('fabricanteDenominacion', newValue || '')}
                onInputChange={(event, newInputValue) => createFormik.setFieldValue('fabricanteDenominacion', newInputValue || '')}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label={`${t('brandLabel')} (Ej: Stanley, Herragro, Bellota, Generico)`}
                    error={createFormik.touched.fabricanteDenominacion && Boolean(createFormik.errors.fabricanteDenominacion)}
                    helperText={createFormik.touched.fabricanteDenominacion && createFormik.errors.fabricanteDenominacion}
                  />
                )}
              />

              {/* Presentación */}
              <Autocomplete
                freeSolo
                options={bases.empaques.map((pr) => pr.denominacion)}
                value={createFormik.values.empaqueDenominacion}
                onChange={(event, newValue) => createFormik.setFieldValue('empaqueDenominacion', newValue || '')}
                onInputChange={(event, newInputValue) => createFormik.setFieldValue('empaqueDenominacion', newInputValue || '')}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label={`${t('presentationLabel')} (Ej: Unidad Suelta, Caja x 100, Galón)`}
                    error={createFormik.touched.empaqueDenominacion && Boolean(createFormik.errors.empaqueDenominacion)}
                    helperText={createFormik.touched.empaqueDenominacion && createFormik.errors.empaqueDenominacion}
                  />
                )}
              />

              {/* Unidad de Medida */}
              <FormControl fullWidth>
                <InputLabel>{t('unitLabel')}</InputLabel>
                <Select
                  value={createFormik.values.medidaBaseClave}
                  label={t('unitLabel')}
                  onChange={(e) => createFormik.setFieldValue('medidaBaseClave', e.target.value)}
                >
                  <MenuItem value="UND">UND — Unidad</MenuItem>
                  <MenuItem value="MTR">MTR — Metro lineal</MenuItem>
                  <MenuItem value="KGM">KGM — Kilogramo</MenuItem>
                  <MenuItem value="GLN">GLN — Galón</MenuItem>
                  <MenuItem value="CJA">CJA — Caja</MenuItem>
                </Select>
              </FormControl>

              {/* Código de Barras */}
              <TextField
                fullWidth
                id="codigoEan"
                name="codigoEan"
                label={t('barcodeLabel')}
                value={createFormik.values.codigoEan}
                onChange={createFormik.handleChange}
                error={createFormik.touched.codigoEan && Boolean(createFormik.errors.codigoEan)}
                helperText={createFormik.touched.codigoEan && createFormik.errors.codigoEan}
              />

              {/* Precio de Venta */}
              <TextField
                fullWidth
                id="tarifaPublico"
                name="tarifaPublico"
                label={t('priceLabel')}
                type="number"
                value={createFormik.values.tarifaPublico}
                onChange={createFormik.handleChange}
                error={createFormik.touched.tarifaPublico && Boolean(createFormik.errors.tarifaPublico)}
                helperText={createFormik.touched.tarifaPublico && createFormik.errors.tarifaPublico}
              />
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenCreateModal(false)}>{t('cancel')}</Button>
            <Button type="submit" variant="contained">{t('create')}</Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
};
