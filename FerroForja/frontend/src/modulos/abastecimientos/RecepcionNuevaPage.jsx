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
  IconButton,
  Grid,
  Card,
  CardContent,
  Divider,
  Alert,
  CircularProgress,
  Stack,
  Autocomplete,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import PrintIcon from '@mui/icons-material/Print';
import { useNavigate } from 'react-router-dom';
import clienteApi from '../../servicios/api/clienteApi';
import { formatearMoneda, formatearVolumen } from '../../utilidades/formatos';

export const RecepcionNuevaPage = () => {
  const navigate = useNavigate();

  const [suministradores, setSuministradores] = useState([]);
  const [suministradorElegido, setSuministradorElegido] = useState(null);
  const [fichas, setFichas] = useState([]);
  const [fichaElegida, setFichaElegida] = useState(null);
  const [volumenInput, setVolumenInput] = useState(1);
  const [costeIndividualInput, setCosteIndividualInput] = useState('');

  const [detalles, setDetalles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [abastecimientoExitosa, setAbastecimientoExitosa] = useState(null);

  useEffect(() => {
    const traerBases = async () => {
      try {
        const [suminRes, refRes] = await Promise.all([
          clienteApi.get('/socios?tipo=SUMINISTRADOR&size=100'),
          clienteApi.get('/catalogo/fichas?habilitado=true&size=100'),
        ]);
        setSuministradores(suminRes.data.content || []);
        setFichas(refRes.data.content || []);

        if (suminRes.data.content?.length > 0) {
          setSuministradorElegido(suminRes.data.content[0]);
        }
      } catch (err) {
        setError('Error al cargar proveedores o catálogo');
      }
    };

    traerBases();
  }, []);

  const handleAnadirDetalle = () => {
    if (!fichaElegida) return;
    const vol = Number(volumenInput);
    const coste = Number(costeIndividualInput);

    if (vol <= 0) {
      setError('La cantidad debe ser mayor a cero');
      return;
    }
    if (coste <= 0) {
      setError('El costo unitario debe ser mayor a cero');
      return;
    }

    setError(null);

    setDetalles([
      ...detalles,
      {
        fichaId: fichaElegida.id,
        codigoEan: fichaElegida.codigoEan,
        articuloDenominacion: fichaElegida.articuloDenominacion,
        fabricanteDenominacion: fichaElegida.fabricanteDenominacion,
        empaqueDenominacion: fichaElegida.empaqueDenominacion,
        medidaBase: fichaElegida.medidaBaseClave,
        volumen: vol,
        costeIndividual: coste,
        subtotal: vol * coste,
      },
    ]);

    setFichaElegida(null);
    setVolumenInput(1);
    setCosteIndividualInput('');
  };

  const handleSuprimirDetalle = (index) => {
    setDetalles(detalles.filter((_, i) => i !== index));
  };

  const totalAbastecimiento = detalles.reduce((sum, l) => sum + l.subtotal, 0);

  const handleRatificarAbastecimiento = async () => {
    if (!suministradorElegido) {
      setError('Debe seleccionar obligatoriamente un proveedor registrado (RN-10)');
      return;
    }
    if (detalles.length === 0) {
      setError('La compra debe contener al menos un artículo');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const payload = {
        suministradorId: suministradorElegido.id,
        detalles: detalles.map((l) => ({
          fichaId: l.fichaId,
          volumen: l.volumen,
          costeIndividual: l.costeIndividual,
        })),
      };

      const response = await clienteApi.post('/abastecimientos', payload);
      setAbastecimientoExitosa(response.data);
      setDetalles([]);

      // Recargar catálogo para actualizar existencias y CPP
      const refRes = await clienteApi.get('/catalogo/fichas?habilitado=true&size=100');
      setFichas(refRes.data.content || []);
    } catch (err) {
      setError(err.response?.data?.detail || 'Error al registrar la compra');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" fontWeight="bold">
          Registro y Recepción de Compras a Proveedores
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Recepción completa en una sola operación (RN-03) y recálculo automático del Costo Promedio Ponderado (RN-04)
        </Typography>
      </Box>

      {abastecimientoExitosa && (
        <Alert
          severity="success"
          icon={<CheckCircleIcon />}
          sx={{ mb: 3 }}
          action={
            <Button
              color="inherit"
              size="small"
              startIcon={<PrintIcon />}
              onClick={() => navigate(`/tiquetes/ABASTECIMIENTO/${abastecimientoExitosa.correlativo}`)}
            >
              Imprimir Comprobante {abastecimientoExitosa.correlativo}
            </Button>
          }
        >
          ¡Compra registrada y recibida con éxito! Consecutivo: <strong>{abastecimientoExitosa.correlativo}</strong>. El inventario y los costos promedio fueron actualizados.
        </Alert>
      )}

      {error && <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>{error}</Alert>}

      <Grid container spacing={3}>
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3, mb: 3 }} elevation={2}>
            <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
              1. Proveedor
            </Typography>
            <Autocomplete
              options={suministradores}
              getOptionLabel={(option) => `${option.tipoIdentificacion} ${option.nroIdentificacion} — ${option.nombreLegal}`}
              value={suministradorElegido}
              onChange={(event, newValue) => setSuministradorElegido(newValue)}
              renderInput={(params) => (
                <TextField {...params} label="Buscar Proveedor por NIT o Nombre" size="small" fullWidth />
              )}
            />
          </Paper>

          <Paper sx={{ p: 3, mb: 3 }} elevation={2}>
            <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
              2. Artículos a Recibir
            </Typography>
            <Grid container spacing={2} alignItems="center">
              <Grid item xs={12} sm={5}>
                <Autocomplete
                  options={fichas}
                  getOptionLabel={(option) => `[${option.codigoEan}] ${option.articuloDenominacion} (${option.fabricanteDenominacion})`}
                  value={fichaElegida}
                  onChange={(event, newValue) => setFichaElegida(newValue)}
                  renderInput={(params) => (
                    <TextField {...params} label="Seleccionar Artículo de Catálogo" size="small" fullWidth />
                  )}
                />
              </Grid>
              <Grid item xs={6} sm={2}>
                <TextField
                  fullWidth
                  size="small"
                  label="Cantidad"
                  type="number"
                  value={volumenInput}
                  onChange={(e) => setVolumenInput(e.target.value)}
                  inputProps={{ min: 1 }}
                />
              </Grid>
              <Grid item xs={6} sm={3}>
                <TextField
                  fullWidth
                  size="small"
                  label="Costo Unitario (COP)"
                  type="number"
                  placeholder="Ej: 15000"
                  value={costeIndividualInput}
                  onChange={(e) => setCosteIndividualInput(e.target.value)}
                />
              </Grid>
              <Grid item xs={12} sm={2}>
                <Button
                  fullWidth
                  variant="contained"
                  startIcon={<AddCircleOutlineIcon />}
                  onClick={handleAnadirDetalle}
                  disabled={!fichaElegida}
                >
                  Añadir
                </Button>
              </Grid>
            </Grid>
          </Paper>

          <TableContainer component={Paper} elevation={2}>
            <Table size="small">
              <TableHead sx={{ bgcolor: 'action.hover' }}>
                <TableRow>
                  <TableCell><strong>Cód.</strong></TableCell>
                  <TableCell><strong>Descripción</strong></TableCell>
                  <TableCell align="center"><strong>Cantidad</strong></TableCell>
                  <TableCell align="right"><strong>Costo Unit. (COP)</strong></TableCell>
                  <TableCell align="right"><strong>Subtotal (COP)</strong></TableCell>
                  <TableCell align="center"><strong>Acción</strong></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {detalles.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} align="center" sx={{ py: 4 }}>
                      No hay artículos agregados a la orden de compra.
                    </TableCell>
                  </TableRow>
                ) : (
                  detalles.map((detalle, index) => (
                    <TableRow key={index} hover>
                      <TableCell sx={{ fontFamily: 'monospace' }}>{detalle.codigoEan}</TableCell>
                      <TableCell>
                        <Typography variant="body2" fontWeight="medium">{detalle.articuloDenominacion}</Typography>
                        <Typography variant="caption" color="text.secondary">{detalle.fabricanteDenominacion} - {detalle.empaqueDenominacion}</Typography>
                      </TableCell>
                      <TableCell align="center">{formatearVolumen(detalle.volumen)} {detalle.medidaBase}</TableCell>
                      <TableCell align="right">{formatearMoneda(detalle.costeIndividual, 2)}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{formatearMoneda(detalle.subtotal)}</TableCell>
                      <TableCell align="center">
                        <IconButton size="small" color="error" onClick={() => handleSuprimirDetalle(index)}>
                          <DeleteIcon fontSize="small" />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card elevation={3} sx={{ borderRadius: 2 }}>
            <CardContent>
              <Typography variant="h6" fontWeight="bold" gutterBottom>
                Resumen de Recepción
              </Typography>
              <Divider sx={{ my: 1.5 }} />

              <Stack spacing={2} sx={{ my: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography variant="body1" color="text.secondary">Total Ítems:</Typography>
                  <Typography variant="body1" fontWeight="bold">
                    {detalles.length}
                  </Typography>
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography variant="body1" color="text.secondary">Unidades Físicas:</Typography>
                  <Typography variant="body1" fontWeight="bold">
                    {detalles.reduce((sum, l) => sum + Number(l.volumen), 0)}
                  </Typography>
                </Box>

                <Divider />

                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
                  <Typography variant="h6">Total Compra:</Typography>
                  <Typography variant="h4" fontWeight="bold" color="text.primary">
                    {formatearMoneda(totalAbastecimiento)}
                  </Typography>
                </Box>

                <Alert severity="info" sx={{ mt: 2 }}>
                  Al confirmar, las existencias ingresarán de inmediato al almacén y el sistema recalculará el <strong>Costo Promedio Ponderado (CPP)</strong> de cada ficha.
                </Alert>

                <Button
                  fullWidth
                  variant="contained"
                  color="primary"
                  size="large"
                  disabled={loading || detalles.length === 0}
                  onClick={handleRatificarAbastecimiento}
                  sx={{ py: 1.5, fontWeight: 'bold' }}
                >
                  {loading ? <CircularProgress size={26} color="inherit" /> : 'Confirmar y Recibir Todo'}
                </Button>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};
