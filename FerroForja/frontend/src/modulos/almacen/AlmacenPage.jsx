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
  Tooltip,
} from '@mui/material';
import HistoryIcon from '@mui/icons-material/History';
import TuneIcon from '@mui/icons-material/Tune';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import SearchIcon from '@mui/icons-material/Search';
import WarningIcon from '@mui/icons-material/Warning';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import clienteApi from '../../servicios/api/clienteApi';
import { formatearMoneda, formatearVolumen, formatearFechaHora } from '../../utilidades/formatos';
import { useSesion } from '../../nucleo/proveedores/SesionProvider';
import { useIdioma } from '../../nucleo/proveedores/IdiomaProvider';

export const AlmacenPage = () => {
  const { hasRole } = useSesion();
  const { t } = useIdioma();
  const isAdmin = hasRole('ROLE_ADMINISTRADOR');

  const [niveles, setNiveles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [soloBajoStock, setSoloBajoStock] = useState(false);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  // Modales
  const [openKardexModal, setOpenKardexModal] = useState(false);
  const [kardexMovements, setKardexMovements] = useState([]);
  const [kardexLoading, setKardexLoading] = useState(false);
  const [selectedRef, setSelectedRef] = useState(null);

  const [openInitialModal, setOpenInitialModal] = useState(false);
  const [openAdjustmentModal, setOpenAdjustmentModal] = useState(false);

  const fetchNiveles = async () => {
    try {
      setLoading(true);
      setError(null);
      const params = new URLSearchParams({ size: 50 });
      if (search) params.append('search', search);
      if (soloBajoStock) params.append('existenciaBaja', 'true');

      const response = await clienteApi.get(`/almacen/existencias?${params.toString()}`);
      setNiveles(response.data.content || []);
    } catch (err) {
      setError('Error al consultar existencias de inventario');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNiveles();
  }, [search, soloBajoStock]);

  const handleOpenKardex = async (item) => {
    setSelectedRef(item);
    setOpenKardexModal(true);
    setKardexLoading(true);
    try {
      const response = await clienteApi.get(`/almacen/fichas/${item.fichaId}/asientos?size=50`);
      setKardexMovements(response.data.content || []);
    } catch (err) {
      setError('Error al consultar Kárdex');
    } finally {
      setKardexLoading(false);
    }
  };

  // Formik Ajuste de Inventario (Solo Administrador - RN-08)
  const adjustmentFormik = useFormik({
    initialValues: {
      tipoReajuste: 'INCREMENTO',
      volumen: '',
      causa: '',
    },
    validationSchema: Yup.object({
      tipoReajuste: Yup.string().required(),
      volumen: Yup.number().positive('Cantidad debe ser positiva').required('Cantidad obligatoria'),
      causa: Yup.string().min(5, 'Mínimo 5 caracteres de justificación').required('Motivo obligatorio (RN-08)'),
    }),
    onSubmit: async (values, { resetForm }) => {
      try {
        await clienteApi.post('/almacen/reajustes', {
          fichaId: selectedRef.fichaId,
          tipoReajuste: values.tipoReajuste,
          volumen: values.volumen,
          causa: values.causa,
        });
        setSuccessMsg(`Ajuste de inventario ejecutado correctamente para ${selectedRef.codigoEan}`);
        setOpenAdjustmentModal(false);
        resetForm();
        fetchNiveles();
      } catch (err) {
        setError(err.response?.data?.detail || 'Error al ejecutar ajuste');
      }
    },
  });

  // Formik Inventario Inicial (Solo Administrador - RN-04)
  const initialStockFormik = useFormik({
    initialValues: {
      volumenApertura: '',
      costeIndividual: '',
    },
    validationSchema: Yup.object({
      volumenApertura: Yup.number().positive('Cantidad debe ser positiva').required('Cantidad inicial requerida'),
      costeIndividual: Yup.number().positive('Costo unitario requerido').required('Costo requerido'),
    }),
    onSubmit: async (values, { resetForm }) => {
      try {
        await clienteApi.post('/almacen/apertura', {
          fichaId: selectedRef.fichaId,
          volumenApertura: values.volumenApertura,
          costeIndividual: values.costeIndividual,
        });
        setSuccessMsg(`Inventario inicial registrado para ${selectedRef.codigoEan}`);
        setOpenInitialModal(false);
        resetForm();
        fetchNiveles();
      } catch (err) {
        setError(err.response?.data?.detail || 'Error al registrar inventario inicial');
      }
    },
  });

  return (
    <Box>
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h5" fontWeight="bold">
            {t('almTitle')}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {t('almSubtitle')}
          </Typography>
        </Box>
        <Stack direction="row" spacing={1}>
          <Button
            variant={soloBajoStock ? 'contained' : 'outlined'}
            color="warning"
            startIcon={<WarningIcon />}
            onClick={() => setSoloBajoStock(!soloBajoStock)}
          >
            {soloBajoStock ? t('showingLowStock') : t('filterLowStock')}
          </Button>
        </Stack>
      </Box>

      {successMsg && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccessMsg(null)}>{successMsg}</Alert>}
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>}

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
                <TableCell align="right"><strong>{t('colStockAvail')}</strong></TableCell>
                {isAdmin && <TableCell align="right"><strong>{t('colAvgCost')}</strong></TableCell>}
                {isAdmin && <TableCell align="right"><strong>{t('colTotalVal')}</strong></TableCell>}
                <TableCell align="center"><strong>{t('colLastUpdate')}</strong></TableCell>
                <TableCell align="center"><strong>{t('colOperations')}</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {niveles.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} align="center" sx={{ py: 3 }}>
                    No se encontraron registros de inventario.
                  </TableCell>
                </TableRow>
              ) : (
                niveles.map((item) => {
                  const vol = Number(item.volumenLibre || 0);
                  const cpp = Number(item.costeMedioPonderado || 0);
                  const totalVal = vol * cpp;
                  return (
                    <TableRow key={item.id} hover>
                      <TableCell sx={{ fontFamily: 'monospace', fontWeight: 'bold' }}>
                        {item.codigoEan}
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" fontWeight="medium">
                          {item.articuloDenominacion}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {item.fabricanteDenominacion} — {item.empaqueDenominacion}
                        </Typography>
                      </TableCell>
                      <TableCell>{item.medidaBase}</TableCell>
                      <TableCell align="right">
                        <Chip
                          label={formatearVolumen(vol)}
                          color={vol <= 0 ? 'error' : vol <= 5 ? 'warning' : 'success'}
                          variant={vol <= 5 ? 'filled' : 'outlined'}
                          sx={{ fontWeight: 'bold' }}
                        />
                      </TableCell>
                      {isAdmin && (
                        <TableCell align="right" sx={{ fontWeight: 'medium' }}>
                          {formatearMoneda(cpp, 2)}
                        </TableCell>
                      )}
                      {isAdmin && (
                        <TableCell align="right" sx={{ fontWeight: 'bold', color: 'text.primary' }}>
                          {formatearMoneda(totalVal)}
                        </TableCell>
                      )}
                      <TableCell align="center">
                        <Typography variant="caption">{formatearFechaHora(item.actualizadoEn)}</Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Stack direction="row" spacing={1} justifyContent="center">
                          {isAdmin && (
                            <Tooltip title="Ver Kárdex (Movimientos Inmutables)">
                              <IconButton
                                size="small"
                                color="info"
                                onClick={() => handleOpenKardex(item)}
                              >
                                <HistoryIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                          )}
                          {isAdmin && (
                            <Tooltip title="Ajuste Manual de Stock (RN-08)">
                              <IconButton
                                size="small"
                                color="warning"
                                onClick={() => {
                                  setSelectedRef(item);
                                  adjustmentFormik.resetForm();
                                  setOpenAdjustmentModal(true);
                                }}
                              >
                                <TuneIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                          )}
                          {isAdmin && vol === 0 && (
                            <Tooltip title="Registrar Inventario Inicial (RN-04)">
                              <IconButton
                                size="small"
                                color="success"
                                onClick={() => {
                                  setSelectedRef(item);
                                  initialStockFormik.resetForm();
                                  setOpenInitialModal(true);
                                }}
                              >
                                <PlayArrowIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                          )}
                        </Stack>
                      </TableCell>
                    </TableRow>
                  );
                })
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Modal Kárdex */}
      <Dialog open={openKardexModal} onClose={() => setOpenKardexModal(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          Kárdex de Movimientos — {selectedRef?.articuloDenominacion} ({selectedRef?.codigoEan})
        </DialogTitle>
        <DialogContent dividers>
          {kardexLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
              <CircularProgress />
            </Box>
          ) : kardexMovements.length === 0 ? (
            <Typography variant="body2" color="text.secondary" align="center" sx={{ py: 3 }}>
              No existen movimientos registrados para este artículo.
            </Typography>
          ) : (
            <TableContainer>
              <Table size="small">
                <TableHead sx={{ bgcolor: 'action.hover' }}>
                  <TableRow>
                    <TableCell>Fecha / Hora</TableCell>
                    <TableCell>Tipo Flujo</TableCell>
                    <TableCell align="right">Cantidad</TableCell>
                    <TableCell align="right">Costo Unit.</TableCell>
                    <TableCell align="right">Saldo Resultante</TableCell>
                    <TableCell>Documento / Motivo</TableCell>
                    <TableCell>Usuario</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {kardexMovements.map((asi) => (
                    <TableRow key={asi.id}>
                      <TableCell>{formatearFechaHora(asi.marcaTiempo)}</TableCell>
                      <TableCell>
                        <Chip
                          label={asi.tipoAsiento}
                          size="small"
                          color={
                            asi.tipoAsiento.includes('ABASTECIMIENTO') || asi.tipoAsiento.includes('ENTRADA') || asi.tipoAsiento.includes('INICIALIZACION')
                              ? 'success'
                              : asi.tipoAsiento.includes('EXPENDIO') || asi.tipoAsiento.includes('SALIDA')
                              ? 'error'
                              : 'default'
                          }
                        />
                      </TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>
                        {formatearVolumen(asi.volumen)}
                      </TableCell>
                      <TableCell align="right">{formatearMoneda(asi.costeIndividualAsiento, 2)}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{formatearVolumen(asi.nivelObtenido)}</TableCell>
                      <TableCell>
                        <Typography variant="caption" display="block">{asi.folioTipo} #{asi.folioId}</Typography>
                        <Typography variant="caption" color="text.secondary">{asi.causa}</Typography>
                      </TableCell>
                      <TableCell>{asi.operarioDenominacion}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenKardexModal(false)}>Cerrar</Button>
        </DialogActions>
      </Dialog>

      {/* Modal Ajuste de Inventario (RN-08) */}
      <Dialog open={openAdjustmentModal} onClose={() => setOpenAdjustmentModal(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Ajuste Manual de Inventario (Solo Admin)</DialogTitle>
        <form onSubmit={adjustmentFormik.handleSubmit}>
          <DialogContent>
            <Typography variant="body2" sx={{ mb: 2 }}>
              Artículo: <strong>{selectedRef?.articuloDenominacion}</strong> (Stock Actual: {selectedRef?.volumenLibre})
            </Typography>
            <FormControl fullWidth margin="normal">
              <InputLabel>Sentido del Ajuste</InputLabel>
              <Select
                id="tipoReajuste"
                name="tipoReajuste"
                value={adjustmentFormik.values.tipoReajuste}
                label="Sentido del Ajuste"
                onChange={adjustmentFormik.handleChange}
              >
                <MenuItem value="INCREMENTO">Incremento (+) Sobrante en conteo físico</MenuItem>
                <MenuItem value="DISMINUCION">Disminución (-) Merma, rotura o pérdida</MenuItem>
              </Select>
            </FormControl>
            <TextField
              fullWidth
              id="volumen"
              name="volumen"
              label="Cantidad a Ajustar"
              type="number"
              margin="normal"
              value={adjustmentFormik.values.volumen}
              onChange={adjustmentFormik.handleChange}
              error={adjustmentFormik.touched.volumen && Boolean(adjustmentFormik.errors.volumen)}
              helperText={adjustmentFormik.touched.volumen && adjustmentFormik.errors.volumen}
            />
            <TextField
              fullWidth
              id="causa"
              name="causa"
              label="Motivo Documentado Obligatorio (RN-08)"
              multiline
              rows={2}
              margin="normal"
              value={adjustmentFormik.values.causa}
              onChange={adjustmentFormik.handleChange}
              error={adjustmentFormik.touched.causa && Boolean(adjustmentFormik.errors.causa)}
              helperText={adjustmentFormik.touched.causa && adjustmentFormik.errors.causa}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenAdjustmentModal(false)}>Cancelar</Button>
            <Button type="submit" variant="contained" color="warning">Aplicar Ajuste</Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* Modal Inventario Inicial (RN-04) */}
      <Dialog open={openInitialModal} onClose={() => setOpenInitialModal(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Carga de Inventario Inicial</DialogTitle>
        <form onSubmit={initialStockFormik.handleSubmit}>
          <DialogContent>
            <Typography variant="body2" sx={{ mb: 2 }}>
              Artículo: <strong>{selectedRef?.articuloDenominacion}</strong> ({selectedRef?.codigoEan})
            </Typography>
            <TextField
              fullWidth
              id="volumenApertura"
              name="volumenApertura"
              label="Cantidad Inicial de Arranque"
              type="number"
              margin="normal"
              value={initialStockFormik.values.volumenApertura}
              onChange={initialStockFormik.handleChange}
              error={initialStockFormik.touched.volumenApertura && Boolean(initialStockFormik.errors.volumenApertura)}
              helperText={initialStockFormik.touched.volumenApertura && initialStockFormik.errors.volumenApertura}
            />
            <TextField
              fullWidth
              id="costeIndividual"
              name="costeIndividual"
              label="Costo Unitario de Referencia (COP)"
              type="number"
              margin="normal"
              value={initialStockFormik.values.costeIndividual}
              onChange={initialStockFormik.handleChange}
              error={initialStockFormik.touched.costeIndividual && Boolean(initialStockFormik.errors.costeIndividual)}
              helperText={initialStockFormik.touched.costeIndividual && initialStockFormik.errors.costeIndividual}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenInitialModal(false)}>Cancelar</Button>
            <Button type="submit" variant="contained" color="success">Asentar Inicial</Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
};
