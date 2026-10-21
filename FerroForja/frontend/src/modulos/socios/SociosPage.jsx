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
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormControlLabel,
  Checkbox,
  Tabs,
  Tab,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import SearchIcon from '@mui/icons-material/Search';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import clienteApi from '../../servicios/api/clienteApi';
import { useIdioma } from '../../nucleo/proveedores/IdiomaProvider';

export const SociosPage = () => {
  const { t } = useIdioma();
  const [socios, setSocios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [filtroTipo, setFiltroTipo] = useState('TODOS');
  const [openModal, setOpenModal] = useState(false);
  const [editingSocio, setEditingSocio] = useState(null);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  const fetchSocios = async () => {
    try {
      setLoading(true);
      setError(null);
      const params = new URLSearchParams({ size: 50 });
      if (search) params.append('search', search);
      if (filtroTipo !== 'TODOS') params.append('tipo', filtroTipo);

      const response = await clienteApi.get(`/socios?${params.toString()}`);
      setSocios(response.data.content || []);
    } catch (err) {
      setError('Error al cargar la lista de terceros');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSocios();
  }, [search, filtroTipo]);

  const formik = useFormik({
    initialValues: {
      tipoIdentificacion: 'CC',
      nroIdentificacion: '',
      nombreLegal: '',
      tipoPersona: 'NATURAL',
      celular: '',
      email: '',
      ubicacion: '',
      esComprador: true,
      esSuministrador: false,
    },
    validationSchema: Yup.object({
      tipoIdentificacion: Yup.string().required('Obligatorio'),
      nroIdentificacion: Yup.string()
        .matches(/^[0-9]+(-[0-9kK])?$/, 'El número de documento debe contener solo dígitos (o guión para NIT ej: 900123456-1)')
        .min(4, 'Mínimo 4 caracteres')
        .max(30, 'Máximo 30 caracteres')
        .required('Número de documento obligatorio'),
      nombreLegal: Yup.string().required('Nombre o Razón Social obligatorio'),
      tipoPersona: Yup.string().required('Obligatorio'),
      celular: Yup.string()
        .matches(/^[0-9+\s-]*$/, 'El teléfono debe contener únicamente números')
        .nullable(),
    }),
    onSubmit: async (values, { resetForm }) => {
      try {
        if (!values.esComprador && !values.esSuministrador) {
          setError('Debe seleccionar al menos un rol comercial: Cliente o Proveedor');
          return;
        }

        if (editingSocio) {
          await clienteApi.put(`/socios/${editingSocio.id}`, values);
          setSuccessMsg('Tercero actualizado correctamente');
        } else {
          await clienteApi.post('/socios', values);
          setSuccessMsg('Tercero registrado correctamente');
        }
        setOpenModal(false);
        resetForm();
        setEditingSocio(null);
        fetchSocios();
      } catch (err) {
        setError(err.response?.data?.detail || 'Error al guardar tercero');
      }
    },
  });

  const handleOpenEdit = (socio) => {
    setEditingSocio(socio);
    formik.setValues({
      tipoIdentificacion: socio.tipoIdentificacion,
      nroIdentificacion: socio.nroIdentificacion,
      nombreLegal: socio.nombreLegal,
      tipoPersona: socio.tipoPersona,
      celular: socio.celular || '',
      email: socio.email || '',
      ubicacion: socio.ubicacion || '',
      esComprador: socio.esComprador,
      esSuministrador: socio.esSuministrador,
    });
    setOpenModal(true);
  };

  const handleOpenCreate = () => {
    setEditingSocio(null);
    formik.resetForm();
    setOpenModal(true);
  };

  return (
    <Box>
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h5" fontWeight="bold">
            Administración de Terceros (Clientes y Proveedores)
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {t('tpSubtitle')}
          </Typography>
        </Box>
        <Button variant="contained" startIcon={<AddIcon />} onClick={handleOpenCreate}>
          {t('newThirdParty')}
        </Button>
      </Box>

      {successMsg && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccessMsg(null)}>{successMsg}</Alert>}
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>}

      <Paper sx={{ mb: 3, p: 2 }}>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems="center">
          <TextField
            fullWidth
            size="small"
            placeholder={t('customerSearchPlaceholder')}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            InputProps={{
              startAdornment: <SearchIcon sx={{ color: 'action.active', mr: 1 }} />,
            }}
          />
          <Tabs
            value={filtroTipo}
            onChange={(e, val) => setFiltroTipo(val)}
            sx={{ minWidth: 320 }}
          >
            <Tab label={t('tabAll')} value="TODOS" />
            <Tab label={t('tabClients')} value="COMPRADOR" />
            <Tab label={t('tabSuppliers')} value="SUMINISTRADOR" />
          </Tabs>
        </Stack>
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
                <TableCell><strong>{t('colId')}</strong></TableCell>
                <TableCell><strong>{t('colBusinessName')}</strong></TableCell>
                <TableCell><strong>{t('colPersonType')}</strong></TableCell>
                <TableCell><strong>{t('colContact')}</strong></TableCell>
                <TableCell align="center"><strong>{t('colPerfiles')}</strong></TableCell>
                <TableCell align="center"><strong>{t('colActions')}</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {socios.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 3 }}>
                    No se encontraron registros de clientes o proveedores.
                  </TableCell>
                </TableRow>
              ) : (
                socios.map((t) => (
                  <TableRow key={t.id} hover>
                    <TableCell sx={{ fontFamily: 'monospace', fontWeight: 'bold' }}>
                      {t.tipoIdentificacion} {t.nroIdentificacion}
                    </TableCell>
                    <TableCell sx={{ fontWeight: 'medium' }}>
                      {t.nombreLegal}
                    </TableCell>
                    <TableCell>{t.tipoPersona}</TableCell>
                    <TableCell>
                      <Typography variant="body2">{t.celular || '-'}</Typography>
                      <Typography variant="caption" color="text.secondary">{t.email || '-'}</Typography>
                    </TableCell>
                    <TableCell align="center">
                      <Stack direction="row" spacing={0.5} justifyContent="center">
                        {t.esComprador && <Chip label="CLIENTE" color="primary" size="small" />}
                        {t.esSuministrador && <Chip label="PROVEEDOR" color="secondary" size="small" />}
                      </Stack>
                    </TableCell>
                    <TableCell align="center">
                      <IconButton size="small" color="secondary" onClick={() => handleOpenEdit(t)}>
                        <EditIcon fontSize="small" />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Modal Crear / Editar */}
      <Dialog open={openModal} onClose={() => setOpenModal(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editingSocio ? 'Editar Tercero' : 'Registrar Nuevo Tercero'}</DialogTitle>
        <form onSubmit={formik.handleSubmit}>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 1 }}>
              <Stack direction="row" spacing={2}>
                <FormControl sx={{ minWidth: 120 }}>
                  <InputLabel>Tipo Doc</InputLabel>
                  <Select
                    id="tipoIdentificacion"
                    name="tipoIdentificacion"
                    value={formik.values.tipoIdentificacion}
                    label="Tipo Doc"
                    onChange={formik.handleChange}
                  >
                    <MenuItem value="CC">CC (Cédula)</MenuItem>
                    <MenuItem value="NIT">NIT (Empresa)</MenuItem>
                    <MenuItem value="CE">CE (Extranjería)</MenuItem>
                    <MenuItem value="PASAPORTE">Pasaporte</MenuItem>
                  </Select>
                </FormControl>
                <TextField
                  fullWidth
                  id="nroIdentificacion"
                  name="nroIdentificacion"
                  label="Número de Documento (Solo dígitos)"
                  value={formik.values.nroIdentificacion}
                  onChange={(e) => {
                    const clean = e.target.value.replace(/[^0-9-]/g, '');
                    formik.setFieldValue('nroIdentificacion', clean);
                  }}
                  error={formik.touched.nroIdentificacion && Boolean(formik.errors.nroIdentificacion)}
                  helperText={formik.touched.nroIdentificacion && formik.errors.nroIdentificacion}
                />
              </Stack>

              <TextField
                fullWidth
                id="nombreLegal"
                name="nombreLegal"
                label="Nombre Completo o Razón Social"
                value={formik.values.nombreLegal}
                onChange={formik.handleChange}
                error={formik.touched.nombreLegal && Boolean(formik.errors.nombreLegal)}
                helperText={formik.touched.nombreLegal && formik.errors.nombreLegal}
              />

              <FormControl fullWidth>
                <InputLabel>Tipo de Persona</InputLabel>
                <Select
                  id="tipoPersona"
                  name="tipoPersona"
                  value={formik.values.tipoPersona}
                  label="Tipo de Persona"
                  onChange={formik.handleChange}
                >
                  <MenuItem value="NATURAL">Persona Natural</MenuItem>
                  <MenuItem value="EMPRESA">Persona Jurídica</MenuItem>
                </Select>
              </FormControl>

              <Stack direction="row" spacing={2}>
                <TextField
                  fullWidth
                  id="celular"
                  name="celular"
                  label="Teléfono / Celular (Solo dígitos)"
                  value={formik.values.celular}
                  onChange={(e) => {
                    const clean = e.target.value.replace(/[^0-9+\s-]/g, '');
                    formik.setFieldValue('celular', clean);
                  }}
                  error={formik.touched.celular && Boolean(formik.errors.celular)}
                  helperText={formik.touched.celular && formik.errors.celular}
                />
                <TextField
                  fullWidth
                  id="email"
                  name="email"
                  label="Correo Electrónico"
                  type="email"
                  value={formik.values.email}
                  onChange={formik.handleChange}
                />
              </Stack>

              <TextField
                fullWidth
                id="ubicacion"
                name="ubicacion"
                label="Dirección Física"
                value={formik.values.ubicacion}
                onChange={formik.handleChange}
              />

              <Box sx={{ p: 1.5, bgcolor: 'background.default', borderRadius: 2 }}>
                <Typography variant="subtitle2" gutterBottom>
                  Roles Comerciales Habilitados (RN-10):
                </Typography>
                <Stack direction="row" spacing={3}>
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={formik.values.esComprador}
                        onChange={(e) => formik.setFieldValue('esComprador', e.target.checked)}
                      />
                    }
                    label="Es Cliente (Habilitado para Ventas)"
                  />
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={formik.values.esSuministrador}
                        onChange={(e) => formik.setFieldValue('esSuministrador', e.target.checked)}
                      />
                    }
                    label="Es Proveedor (Habilitado para Compras)"
                  />
                </Stack>
              </Box>
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenModal(false)}>Cancelar</Button>
            <Button type="submit" variant="contained">
              {editingSocio ? 'Guardar Cambios' : 'Registrar'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
};
