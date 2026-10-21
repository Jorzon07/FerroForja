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
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Autocomplete,
  Tooltip,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import PrintIcon from '@mui/icons-material/Print';
import AssignmentReturnIcon from '@mui/icons-material/AssignmentReturn';
import { useNavigate } from 'react-router-dom';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import clienteApi from '../../servicios/api/clienteApi';
import { formatearMoneda, formatearVolumen, formatearFechaHora } from '../../utilidades/formatos';

export const RestitucionesPage = () => {
  const navigate = useNavigate();

  const [restituciones, setRestituciones] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  // Estados para modal de nueva devolución
  const [openModal, setOpenModal] = useState(false);
  const [tipoRestitucion, setTipoRestitucion] = useState('NO_VINCULADA');
  const [fichas, setFichas] = useState([]);
  const [compradores, setCompradores] = useState([]);
  const [expendios, setExpendios] = useState([]);

  const [selectedRef, setSelectedRef] = useState(null);
  const [selectedCmp, setSelectedCmp] = useState(null);
  const [selectedExpendio, setSelectedExpendio] = useState(null);
  const [volumenDevuelta, setVolumenDevuelta] = useState(1);
  const [causa, setCausa] = useState('');

  const fetchRestituciones = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await clienteApi.get('/restituciones?size=50');
      setRestituciones(response.data.content || []);
    } catch (err) {
      setError('Error al consultar devoluciones');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRestituciones();
  }, []);

  const handleOpenCreate = async () => {
    try {
      const [refRes, cmpRes, expRes] = await Promise.all([
        clienteApi.get('/catalogo/fichas?habilitado=true&size=100'),
        clienteApi.get('/socios?tipo=COMPRADOR&size=100'),
        clienteApi.get('/expendios?situacion=COMPLETADA&size=50'),
      ]);
      setFichas(refRes.data.content || []);
      setCompradores(cmpRes.data.content || []);
      setExpendios(expRes.data.content || []);
      setOpenModal(true);
    } catch (err) {
      setError('Error al preparar formulario de devolución');
    }
  };

  const handleRatificarRestitucion = async () => {
    if (!selectedRef) {
      setError('Debe seleccionar un producto');
      return;
    }
    if (Number(volumenDevuelta) <= 0) {
      setError('La cantidad devuelta debe ser mayor a cero');
      return;
    }
    if (!causa || causa.trim().length < 5) {
      setError('Debe ingresar un motivo de al menos 5 caracteres');
      return;
    }
    if (tipoRestitucion === 'VINCULADA' && !selectedExpendio) {
      setError('Para una devolución VINCULADA debe seleccionar la venta original');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      // El backend deriva el tipo (VINCULADA/NO_VINCULADA) desde expendioId; no se envía tipoRestitucion.
      const payload = {
        expendioId: tipoRestitucion === 'VINCULADA' ? selectedExpendio?.id : null,
        compradorId: selectedCmp?.id || null,
        causa,
        detalles: [
          {
            fichaId: selectedRef.id,
            volumen: Number(volumenDevuelta),
          },
        ],
      };

      const response = await clienteApi.post('/restituciones', payload);
      setSuccessMsg(`Devolución ${response.data.correlativo} registrada. Las existencias fueron reincorporadas.`);
      setOpenModal(false);
      setSelectedRef(null);
      setSelectedExpendio(null);
      setCausa('');
      fetchRestituciones();
    } catch (err) {
      setError(err.response?.data?.detail || 'Error al procesar la devolución');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h5" fontWeight="bold">
            Devoluciones de Mercancía de Clientes
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Reincorporación automática de mercancía al stock disponible valorada a costo histórico o CPP (RN-07)
          </Typography>
        </Box>
        <Button variant="contained" startIcon={<AddIcon />} onClick={handleOpenCreate}>
          Nueva Devolución
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
                <TableCell><strong>Tipo</strong></TableCell>
                <TableCell><strong>Venta Original</strong></TableCell>
                <TableCell><strong>Cliente</strong></TableCell>
                <TableCell><strong>Motivo</strong></TableCell>
                <TableCell align="center"><strong>Estado</strong></TableCell>
                <TableCell align="center"><strong>Imprimir</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {restituciones.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} align="center" sx={{ py: 3 }}>
                    No se registran devoluciones de clientes en el sistema.
                  </TableCell>
                </TableRow>
              ) : (
                restituciones.map((d) => (
                  <TableRow key={d.id} hover>
                    <TableCell sx={{ fontFamily: 'monospace', fontWeight: 'bold' }}>
                      {d.correlativo}
                    </TableCell>
                    <TableCell>{formatearFechaHora(d.marcaTiempo)}</TableCell>
                    <TableCell>
                      <Chip
                        label={d.tipoRestitucion}
                        size="small"
                        color={d.tipoRestitucion === 'VINCULADA' ? 'primary' : 'default'}
                      />
                    </TableCell>
                    <TableCell>{d.expendioCorrelativo || 'N/A'}</TableCell>
                    <TableCell>{d.compradorDenominacion || 'Sin asociar'}</TableCell>
                    <TableCell sx={{ maxWidth: 200, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                      {d.causa}
                    </TableCell>
                    <TableCell align="center">
                      <Chip label={d.situacion} color="success" size="small" />
                    </TableCell>
                    <TableCell align="center">
                      <Tooltip title="Imprimir Comprobante de Devolución">
                        <IconButton
                          size="small"
                          color="info"
                          onClick={() => navigate(`/tiquetes/RESTITUCION/${d.correlativo}`)}
                        >
                          <PrintIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Modal Registrar Devolución */}
      <Dialog open={openModal} onClose={() => setOpenModal(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Registrar Devolución de Cliente</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <FormControl fullWidth size="small">
              <InputLabel>Modalidad de Devolución</InputLabel>
              <Select
                value={tipoRestitucion}
                label="Modalidad de Devolución"
                onChange={(e) => setTipoRestitucion(e.target.value)}
              >
                <MenuItem value="NO_VINCULADA">No Vinculada (Reingresa a Costo Promedio Vigente)</MenuItem>
                <MenuItem value="VINCULADA">Vinculada a Venta Original (Reingresa a Costo Histórico)</MenuItem>
              </Select>
            </FormControl>

            {tipoRestitucion === 'VINCULADA' && (
              <Autocomplete
                options={expendios}
                getOptionLabel={(option) => `${option.correlativo} — ${option.compradorDenominacion} (${formatearMoneda(option.total)})`}
                value={selectedExpendio}
                onChange={(event, newValue) => setSelectedExpendio(newValue)}
                renderInput={(params) => (
                  <TextField {...params} label="Buscar y Seleccionar Venta Original" size="small" fullWidth />
                )}
              />
            )}

            <Autocomplete
              options={compradores}
              getOptionLabel={(option) => `${option.nroIdentificacion} — ${option.nombreLegal}`}
              value={selectedCmp}
              onChange={(event, newValue) => setSelectedCmp(newValue)}
              renderInput={(params) => (
                <TextField {...params} label="Cliente que devuelve" size="small" fullWidth />
              )}
            />

            <Autocomplete
              options={fichas}
              getOptionLabel={(option) => `[${option.codigoEan}] ${option.articuloDenominacion} (${option.fabricanteDenominacion})`}
              value={selectedRef}
              onChange={(event, newValue) => setSelectedRef(newValue)}
              renderInput={(params) => (
                <TextField {...params} label="Artículo que reingresa a almacén" size="small" fullWidth />
              )}
            />

            <TextField
              fullWidth
              size="small"
              label="Cantidad a Devolver"
              type="number"
              value={volumenDevuelta}
              onChange={(e) => setVolumenDevuelta(e.target.value)}
              inputProps={{ min: 1 }}
            />

            <TextField
              fullWidth
              label="Motivo o Justificación Obligatoria"
              multiline
              rows={2}
              value={causa}
              onChange={(e) => setCausa(e.target.value)}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenModal(false)}>Cancelar</Button>
          <Button variant="contained" onClick={handleRatificarRestitucion} disabled={loading}>
            Reintegrar al Inventario
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};
