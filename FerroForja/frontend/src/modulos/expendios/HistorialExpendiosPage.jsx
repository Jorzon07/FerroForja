import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  IconButton,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  CircularProgress,
  Alert,
  Stack,
  Tooltip,
} from '@mui/material';
import VisibilityIcon from '@mui/icons-material/Visibility';
import PrintIcon from '@mui/icons-material/Print';
import CancelIcon from '@mui/icons-material/Cancel';
import { useNavigate } from 'react-router-dom';
import clienteApi from '../../servicios/api/clienteApi';
import { formatearMoneda, formatearVolumen, formatearFechaHora } from '../../utilidades/formatos';
import { useSesion } from '../../nucleo/proveedores/SesionProvider';

export const HistorialExpendiosPage = () => {
  const { hasRole } = useSesion();
  const isAdmin = hasRole('ROLE_ADMINISTRADOR');
  const navigate = useNavigate();

  const [expendios, setExpendios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  // Modales
  const [selectedExpendio, setSelectedExpendio] = useState(null);
  const [openDetailModal, setOpenDetailModal] = useState(false);
  const [openCancelModal, setOpenCancelModal] = useState(false);
  const [causaAnulacion, setCausaAnulacion] = useState('');

  const fetchExpendios = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await clienteApi.get('/expendios?size=50');
      setExpendios(response.data.content || []);
    } catch (err) {
      setError('Error al consultar historial de ventas');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchExpendios();
  }, []);

  const handleOpenDetail = (expendio) => {
    setSelectedExpendio(expendio);
    setOpenDetailModal(true);
  };

  const handleOpenCancel = (expendio) => {
    setSelectedExpendio(expendio);
    setCausaAnulacion('');
    setOpenCancelModal(true);
  };

  const handleConfirmCancel = async () => {
    if (!causaAnulacion || causaAnulacion.trim().length < 5) {
      setError('El motivo de cancelación debe tener al menos 5 caracteres');
      return;
    }

    try {
      await clienteApi.post(`/expendios/${selectedExpendio.id}/anular`, {
        causa: causaAnulacion,
      });
      setSuccessMsg(`Venta ${selectedExpendio.correlativo} cancelada exitosamente y stock reincorporado`);
      setOpenCancelModal(false);
      fetchExpendios();
    } catch (err) {
      setError(err.response?.data?.detail || 'Error al cancelar la venta');
    }
  };

  return (
    <Box>
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h5" fontWeight="bold">
            Historial de Ventas de Mostrador
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Consulta de comprobantes emitidos, líneas con costos históricos congelados y anulación autorizada
          </Typography>
        </Box>
        <Button variant="contained" color="secondary" onClick={() => navigate('/expendios/nueva')}>
          Nueva Venta
        </Button>
      </Box>

      {successMsg && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccessMsg(null)}>{successMsg}</Alert>}
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>}

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <TableContainer component={Paper} elevation={2}>
          <Table>
            <TableHead sx={{ bgcolor: 'action.hover' }}>
              <TableRow>
                <TableCell><strong>Consecutivo</strong></TableCell>
                <TableCell><strong>Fecha / Hora</strong></TableCell>
                <TableCell><strong>Cliente</strong></TableCell>
                <TableCell align="right"><strong>Total (COP)</strong></TableCell>
                <TableCell align="center"><strong>Estado</strong></TableCell>
                <TableCell><strong>Vendedor</strong></TableCell>
                <TableCell align="center"><strong>Acciones</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {expendios.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} align="center" sx={{ py: 3 }}>
                    No se registran ventas en el sistema.
                  </TableCell>
                </TableRow>
              ) : (
                expendios.map((v) => (
                  <TableRow key={v.id} hover>
                    <TableCell sx={{ fontFamily: 'monospace', fontWeight: 'bold' }}>
                      {v.correlativo}
                    </TableCell>
                    <TableCell>{formatearFechaHora(v.marcaTiempo)}</TableCell>
                    <TableCell>
                      <Typography variant="body2" fontWeight="medium">{v.compradorDenominacion}</Typography>
                      <Typography variant="caption" color="text.secondary">{v.compradorFolio}</Typography>
                    </TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold', color: 'text.primary' }}>
                      {formatearMoneda(v.total)}
                    </TableCell>
                    <TableCell align="center">
                      <Chip
                        label={v.situacion}
                        color={v.situacion === 'COMPLETADA' ? 'success' : 'default'}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>{v.operarioDenominacion}</TableCell>
                    <TableCell align="center">
                      <Stack direction="row" spacing={1} justifyContent="center">
                        <Tooltip title="Ver Detalle y Costo Histórico">
                          <IconButton size="small" color="secondary" onClick={() => handleOpenDetail(v)}>
                            <VisibilityIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Imprimir Comprobante">
                          <IconButton
                            size="small"
                            color="info"
                            onClick={() => navigate(`/tiquetes/EXPENDIO/${v.correlativo}`)}
                          >
                            <PrintIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        {isAdmin && v.situacion === 'COMPLETADA' && (
                          <Tooltip title="Cancelar Venta (Reversión de Stock)">
                            <IconButton size="small" color="error" onClick={() => handleOpenCancel(v)}>
                              <CancelIcon fontSize="small" />
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

      {/* Modal Detalle de Venta */}
      <Dialog open={openDetailModal} onClose={() => setOpenDetailModal(false)} maxWidth="md" fullWidth>
        <DialogTitle>Detalle de Venta: {selectedExpendio?.correlativo}</DialogTitle>
        <DialogContent dividers>
          <Box sx={{ mb: 2 }}>
            <Typography variant="body2"><strong>Cliente:</strong> {selectedExpendio?.compradorDenominacion} ({selectedExpendio?.compradorFolio})</Typography>
            <Typography variant="body2"><strong>Fecha:</strong> {formatearFechaHora(selectedExpendio?.marcaTiempo)}</Typography>
            <Typography variant="body2"><strong>Estado:</strong> {selectedExpendio?.situacion}</Typography>
            {selectedExpendio?.causaAnulacion && (
              <Alert severity="error" sx={{ mt: 1 }}>
                Motivo de Cancelación: {selectedExpendio.causaAnulacion}
              </Alert>
            )}
          </Box>

          <TableContainer>
            <Table size="small">
              <TableHead sx={{ bgcolor: 'action.hover' }}>
                <TableRow>
                  <TableCell>Cód. Barras</TableCell>
                  <TableCell>Producto</TableCell>
                  <TableCell align="center">Cantidad</TableCell>
                  <TableCell align="right">Precio Unit.</TableCell>
                  {isAdmin && <TableCell align="right">Costo Histórico Unit. (RN-04)</TableCell>}
                  <TableCell align="right">Subtotal</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {selectedExpendio?.detalles?.map((l) => (
                  <TableRow key={l.id}>
                    <TableCell sx={{ fontFamily: 'monospace' }}>{l.codigoEan}</TableCell>
                    <TableCell>{l.articuloDenominacion}</TableCell>
                    <TableCell align="center">{formatearVolumen(l.volumen)}</TableCell>
                    <TableCell align="right">{formatearMoneda(l.tarifaIndividual)}</TableCell>
                    {isAdmin && (
                      <TableCell align="right" sx={{ color: 'text.secondary' }}>
                        {formatearMoneda(l.costeCongeladoIndividual, 2)}
                      </TableCell>
                    )}
                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>{formatearMoneda(l.subtotal)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          <Box sx={{ mt: 2, textAlign: 'right' }}>
            <Typography variant="h6" fontWeight="bold">Total: {formatearMoneda(selectedExpendio?.total)}</Typography>
            <Typography variant="body2" color="text.secondary">Efectivo Recibido: {formatearMoneda(selectedExpendio?.importeAbonadoEfectivo)}</Typography>
            <Typography variant="body2" color="text.secondary">Cambio Devuelto: {formatearMoneda(selectedExpendio?.vueltoEntregado)}</Typography>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDetailModal(false)}>Cerrar</Button>
          <Button
            variant="contained"
            startIcon={<PrintIcon />}
            onClick={() => navigate(`/tiquetes/EXPENDIO/${selectedExpendio?.correlativo}`)}
          >
            Imprimir
          </Button>
        </DialogActions>
      </Dialog>

      {/* Modal Cancelar Venta */}
      <Dialog open={openCancelModal} onClose={() => setOpenCancelModal(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Cancelar Venta {selectedExpendio?.correlativo}</DialogTitle>
        <DialogContent>
          <Alert severity="warning" sx={{ mb: 2 }}>
            Esta operación anulará el comprobante y reincorporará las existencias al inventario.
          </Alert>
          <TextField
            fullWidth
            label="Motivo Obligatorio de Cancelación"
            multiline
            rows={3}
            value={causaAnulacion}
            onChange={(e) => setCausaAnulacion(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenCancelModal(false)}>Volver</Button>
          <Button variant="contained" color="error" onClick={handleConfirmCancel}>
            Confirmar Anulación
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};
