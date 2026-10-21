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

export const HistorialAbastecimientosPage = () => {
  const { hasRole } = useSesion();
  const isAdmin = hasRole('ROLE_ADMINISTRADOR');
  const navigate = useNavigate();

  const [abastecimientos, setAbastecimientos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  // Modales
  const [selectedAbastecimiento, setSelectedAbastecimiento] = useState(null);
  const [openDetailModal, setOpenDetailModal] = useState(false);
  const [openCancelModal, setOpenCancelModal] = useState(false);
  const [causaAnulacion, setCausaAnulacion] = useState('');

  const fetchAbastecimientos = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await clienteApi.get('/abastecimientos?size=50');
      setAbastecimientos(response.data.content || []);
    } catch (err) {
      setError('Error al consultar historial de compras');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAbastecimientos();
  }, []);

  const handleOpenDetail = (abastecimiento) => {
    setSelectedAbastecimiento(abastecimiento);
    setOpenDetailModal(true);
  };

  const handleOpenCancel = (abastecimiento) => {
    setSelectedAbastecimiento(abastecimiento);
    setCausaAnulacion('');
    setOpenCancelModal(true);
  };

  const handleConfirmCancel = async () => {
    if (!causaAnulacion || causaAnulacion.trim().length < 5) {
      setError('El motivo de cancelación debe tener al menos 5 caracteres');
      return;
    }

    try {
      await clienteApi.post(`/abastecimientos/${selectedAbastecimiento.id}/anular`, {
        causa: causaAnulacion,
      });
      setSuccessMsg(`Compra ${selectedAbastecimiento.correlativo} cancelada exitosamente y stock revertido`);
      setOpenCancelModal(false);
      fetchAbastecimientos();
    } catch (err) {
      setError(err.response?.data?.detail || 'Error al cancelar la compra. Verifique existencias.');
    }
  };

  return (
    <Box>
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h5" fontWeight="bold">
            Historial de Compras y Recepción de Mercancía
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Consulta de compras recibidas, líneas de abastecimiento y costos unitarios de adquisición
          </Typography>
        </Box>
        <Button variant="contained" onClick={() => navigate('/abastecimientos/nueva')}>
          Nueva Compra
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
                <TableCell><strong>Proveedor</strong></TableCell>
                <TableCell align="right"><strong>Total (COP)</strong></TableCell>
                <TableCell align="center"><strong>Estado</strong></TableCell>
                <TableCell><strong>Responsable</strong></TableCell>
                <TableCell align="center"><strong>Acciones</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {abastecimientos.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} align="center" sx={{ py: 3 }}>
                    No se registran compras en el sistema.
                  </TableCell>
                </TableRow>
              ) : (
                abastecimientos.map((c) => (
                  <TableRow key={c.id} hover>
                    <TableCell sx={{ fontFamily: 'monospace', fontWeight: 'bold' }}>
                      {c.correlativo}
                    </TableCell>
                    <TableCell>{formatearFechaHora(c.marcaTiempo)}</TableCell>
                    <TableCell>
                      <Typography variant="body2" fontWeight="medium">{c.suministradorNombreLegal}</Typography>
                      <Typography variant="caption" color="text.secondary">{c.suministradorFolio}</Typography>
                    </TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold', color: 'text.primary' }}>
                      {formatearMoneda(c.total)}
                    </TableCell>
                    <TableCell align="center">
                      <Chip
                        label={c.situacion}
                        color={c.situacion === 'RECIBIDA' ? 'success' : 'default'}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>{c.operarioDenominacion}</TableCell>
                    <TableCell align="center">
                      <Stack direction="row" spacing={1} justifyContent="center">
                        <Tooltip title="Ver Detalle de Compra">
                          <IconButton size="small" color="secondary" onClick={() => handleOpenDetail(c)}>
                            <VisibilityIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Imprimir Comprobante">
                          <IconButton
                            size="small"
                            color="info"
                            onClick={() => navigate(`/tiquetes/ABASTECIMIENTO/${c.correlativo}`)}
                          >
                            <PrintIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        {isAdmin && c.situacion === 'RECIBIDA' && (
                          <Tooltip title="Cancelar Compra (Reversión de Stock)">
                            <IconButton size="small" color="error" onClick={() => handleOpenCancel(c)}>
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

      {/* Modal Detalle de Compra */}
      <Dialog open={openDetailModal} onClose={() => setOpenDetailModal(false)} maxWidth="md" fullWidth>
        <DialogTitle>Detalle de Compra: {selectedAbastecimiento?.correlativo}</DialogTitle>
        <DialogContent dividers>
          <Box sx={{ mb: 2 }}>
            <Typography variant="body2"><strong>Proveedor:</strong> {selectedAbastecimiento?.suministradorNombreLegal} ({selectedAbastecimiento?.suministradorFolio})</Typography>
            <Typography variant="body2"><strong>Fecha de Recepción:</strong> {formatearFechaHora(selectedAbastecimiento?.marcaTiempo)}</Typography>
            <Typography variant="body2"><strong>Estado:</strong> {selectedAbastecimiento?.situacion}</Typography>
            {selectedAbastecimiento?.causaAnulacion && (
              <Alert severity="error" sx={{ mt: 1 }}>
                Motivo de Cancelación: {selectedAbastecimiento.causaAnulacion}
              </Alert>
            )}
          </Box>

          <TableContainer>
            <Table size="small">
              <TableHead sx={{ bgcolor: 'action.hover' }}>
                <TableRow>
                  <TableCell>Cód. Barras</TableCell>
                  <TableCell>Producto Recibido</TableCell>
                  <TableCell align="center">Cantidad</TableCell>
                  <TableCell align="right">Costo Unit. Compra</TableCell>
                  <TableCell align="right">Subtotal</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {selectedAbastecimiento?.detalles?.map((l) => (
                  <TableRow key={l.id}>
                    <TableCell sx={{ fontFamily: 'monospace' }}>{l.codigoEan}</TableCell>
                    <TableCell>{l.articuloDenominacion}</TableCell>
                    <TableCell align="center">{formatearVolumen(l.volumen)}</TableCell>
                    <TableCell align="right">{formatearMoneda(l.costeIndividual, 2)}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>{formatearMoneda(l.subtotal)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          <Box sx={{ mt: 2, textAlign: 'right' }}>
            <Typography variant="h6" fontWeight="bold">Total Compra: {formatearMoneda(selectedAbastecimiento?.total)}</Typography>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDetailModal(false)}>Cerrar</Button>
          <Button
            variant="contained"
            startIcon={<PrintIcon />}
            onClick={() => navigate(`/tiquetes/ABASTECIMIENTO/${selectedAbastecimiento?.correlativo}`)}
          >
            Imprimir
          </Button>
        </DialogActions>
      </Dialog>

      {/* Modal Cancelar Compra */}
      <Dialog open={openCancelModal} onClose={() => setOpenCancelModal(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Cancelar Compra {selectedAbastecimiento?.correlativo}</DialogTitle>
        <DialogContent>
          <Alert severity="warning" sx={{ mb: 2 }}>
            Esta operación verificará que las existencias no hayan sido consumidas o vendidas para revertir el stock.
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
            Confirmar Reversión
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};
