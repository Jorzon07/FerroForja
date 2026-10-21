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
import AddShoppingCartIcon from '@mui/icons-material/AddShoppingCart';
import PrintIcon from '@mui/icons-material/Print';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { useNavigate } from 'react-router-dom';
import clienteApi from '../../servicios/api/clienteApi';
import { formatearMoneda, formatearVolumen } from '../../utilidades/formatos';
import { useIdioma } from '../../nucleo/proveedores/IdiomaProvider';

export const MostradorPage = () => {
  const navigate = useNavigate();
  const { t } = useIdioma();

  // Estados de clientes y catálogo
  const [compradores, setCompradores] = useState([]);
  const [compradorElegido, setCompradorElegido] = useState(null);
  const [fichas, setFichas] = useState([]);
  const [fichaElegida, setFichaElegida] = useState(null);
  const [volumenInput, setVolumenInput] = useState(1);

  // Carrito de venta
  const [detalles, setDetalles] = useState([]);
  const [importeEfectivo, setImporteEfectivo] = useState('');

  // Estados UI
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [expendioExitosa, setExpendioExitosa] = useState(null);

  useEffect(() => {
    const traerBases = async () => {
      try {
        const [cmpRes, refRes] = await Promise.all([
          clienteApi.get('/socios?tipo=COMPRADOR&size=100'),
          clienteApi.get('/catalogo/fichas?habilitado=true&size=100'),
        ]);
        setCompradores(cmpRes.data.content || []);
        setFichas(refRes.data.content || []);

        if (cmpRes.data.content?.length > 0) {
          setCompradorElegido(cmpRes.data.content[0]);
        }
      } catch (err) {
        setError('Error al cargar clientes o artículos de catálogo');
      }
    };

    traerBases();
  }, []);

  const handleAnadirDetalle = () => {
    if (!fichaElegida) return;
    const vol = Number(volumenInput);
    if (vol <= 0) {
      setError('La cantidad debe ser mayor a cero');
      return;
    }

    const existenciaActual = Number(fichaElegida.existenciaLibre || 0);
    const yaEnCesta = detalles.find((l) => l.fichaId === fichaElegida.id);
    const volTotal = (yaEnCesta ? yaEnCesta.volumen : 0) + vol;

    if (volTotal > existenciaActual) {
      setError(`Stock insuficiente para ${fichaElegida.codigoEan}. Disponible: ${existenciaActual}, solicitado: ${volTotal}`);
      return;
    }

    setError(null);

    if (yaEnCesta) {
      setDetalles(
        detalles.map((l) =>
          l.fichaId === fichaElegida.id
            ? { ...l, volumen: volTotal, subtotal: volTotal * l.tarifaIndividual }
            : l
        )
      );
    } else {
      setDetalles([
        ...detalles,
        {
          fichaId: fichaElegida.id,
          codigoEan: fichaElegida.codigoEan,
          articuloDenominacion: fichaElegida.articuloDenominacion,
          fabricanteDenominacion: fichaElegida.fabricanteDenominacion,
          empaqueDenominacion: fichaElegida.empaqueDenominacion,
          medidaBase: fichaElegida.medidaBaseClave,
          tarifaIndividual: Number(fichaElegida.tarifaPublico),
          volumen: vol,
          subtotal: vol * Number(fichaElegida.tarifaPublico),
        },
      ]);
    }

    setFichaElegida(null);
    setVolumenInput(1);
  };

  const handleSuprimirDetalle = (refId) => {
    setDetalles(detalles.filter((l) => l.fichaId !== refId));
  };

  const totalExpendio = detalles.reduce((sum, l) => sum + l.subtotal, 0);
  const efectivoNum = Number(importeEfectivo) || 0;
  const vuelto = efectivoNum >= totalExpendio ? efectivoNum - totalExpendio : 0;

  const handleRatificarExpendio = async () => {
    if (!compradorElegido) {
      setError('Debe seleccionar obligatoriamente un cliente registrado (RN-05)');
      return;
    }
    if (detalles.length === 0) {
      setError('El carrito no contiene ningún producto');
      return;
    }
    if (efectivoNum < totalExpendio) {
      setError('El monto en efectivo recibido es inferior al total a pagar (RN-06)');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const payload = {
        compradorId: compradorElegido.id,
        importeAbonadoEfectivo: efectivoNum,
        detalles: detalles.map((l) => ({
          fichaId: l.fichaId,
          volumen: l.volumen,
        })),
      };

      const response = await clienteApi.post('/expendios', payload);
      setExpendioExitosa(response.data);
      setDetalles([]);
      setImporteEfectivo('');

      // Recargar catálogo para actualizar existencias visibles
      const refRes = await clienteApi.get('/catalogo/fichas?habilitado=true&size=100');
      setFichas(refRes.data.content || []);
    } catch (err) {
      const status = err.response?.status;
      const detail =
        err.response?.data?.detail || err.response?.data?.message || err.response?.data?.title;
      if (status === 403) {
        setError(
          (detail ? `${detail} ` : '') +
            '(HTTP 403: sesión sin token CSRF o sin permisos. Recargue la página y vuelva a iniciar sesión si persiste.)'
        );
      } else if (status === 401) {
        setError('Sesión expirada o no autenticada (HTTP 401). Será redirigido al inicio de sesión.');
      } else {
        setError(detail || err.message || 'Error al procesar la venta');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" fontWeight="bold">
          {t('posTitle')}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          {t('posSubtitle')}
        </Typography>
      </Box>

      {expendioExitosa && (
        <Alert
          severity="success"
          icon={<CheckCircleIcon />}
          sx={{ mb: 3 }}
          action={
            <Button
              color="inherit"
              size="small"
              startIcon={<PrintIcon />}
              onClick={() => navigate(`/tiquetes/EXPENDIO/${expendioExitosa.correlativo}`)}
            >
              {t('printReceipt')} {expendioExitosa.correlativo}
            </Button>
          }
        >
          {t('saleSuccessAlert')} <strong>{expendioExitosa.correlativo}</strong>. {t('changeAlert')} <strong>{formatearMoneda(expendioExitosa.vueltoEntregado)}</strong>.
        </Alert>
      )}

      {error && <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>{error}</Alert>}

      <Grid container spacing={3}>
        {/* Panel Izquierdo: Selección de Cliente y Productos */}
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3, mb: 3 }} elevation={2}>
            <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
              {t('customerStep')}
            </Typography>
            <Autocomplete
              options={compradores}
              getOptionLabel={(option) => `${option.tipoIdentificacion} ${option.nroIdentificacion} — ${option.nombreLegal}`}
              value={compradorElegido}
              onChange={(event, newValue) => setCompradorElegido(newValue)}
              renderInput={(params) => (
                <TextField {...params} label={t('customerSearchPlaceholder')} size="small" fullWidth />
              )}
            />
          </Paper>

          <Paper sx={{ p: 3, mb: 3 }} elevation={2}>
            <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
              {t('itemSearchStep')}
            </Typography>
            <Grid container spacing={2} alignItems="center">
              <Grid item xs={12} sm={7}>
                <Autocomplete
                  options={fichas}
                  getOptionLabel={(option) => `[${option.codigoEan}] ${option.articuloDenominacion} (${option.fabricanteDenominacion}) — ${t('colStock')}: ${formatearVolumen(option.existenciaLibre)}`}
                  value={fichaElegida}
                  onChange={(event, newValue) => setFichaElegida(newValue)}
                  renderInput={(params) => (
                    <TextField {...params} label={t('itemSearchPlaceholder')} size="small" fullWidth />
                  )}
                />
              </Grid>
              <Grid item xs={6} sm={2}>
                <TextField
                  fullWidth
                  size="small"
                  label={t('quantity')}
                  type="number"
                  value={volumenInput}
                  onChange={(e) => setVolumenInput(e.target.value)}
                  inputProps={{ min: 1 }}
                />
              </Grid>
              <Grid item xs={6} sm={3}>
                <Button
                  fullWidth
                  variant="contained"
                  startIcon={<AddShoppingCartIcon />}
                  onClick={handleAnadirDetalle}
                  disabled={!fichaElegida}
                >
                  {t('add')}
                </Button>
              </Grid>
            </Grid>
          </Paper>

          <TableContainer component={Paper} elevation={2}>
            <Table size="small">
              <TableHead sx={{ bgcolor: 'action.hover' }}>
                <TableRow>
                  <TableCell><strong>{t('colItem')}</strong></TableCell>
                  <TableCell><strong>{t('colDesc')}</strong></TableCell>
                  <TableCell align="center"><strong>{t('colVol')}</strong></TableCell>
                  <TableCell align="right"><strong>{t('colUnitPrice')}</strong></TableCell>
                  <TableCell align="right"><strong>{t('colSubtotal')}</strong></TableCell>
                  <TableCell align="center"><strong>{t('colRemove')}</strong></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {detalles.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} align="center" sx={{ py: 4 }}>
                      {t('cartEmpty')}
                    </TableCell>
                  </TableRow>
                ) : (
                  detalles.map((detalle) => (
                    <TableRow key={detalle.fichaId} hover>
                      <TableCell sx={{ fontFamily: 'monospace' }}>{detalle.codigoEan}</TableCell>
                      <TableCell>
                        <Typography variant="body2" fontWeight="medium">{detalle.articuloDenominacion}</Typography>
                        <Typography variant="caption" color="text.secondary">{detalle.fabricanteDenominacion} - {detalle.empaqueDenominacion}</Typography>
                      </TableCell>
                      <TableCell align="center">{formatearVolumen(detalle.volumen)} {detalle.medidaBase}</TableCell>
                      <TableCell align="right">{formatearMoneda(detalle.tarifaIndividual)}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{formatearMoneda(detalle.subtotal)}</TableCell>
                      <TableCell align="center">
                        <IconButton size="small" color="error" onClick={() => handleSuprimirDetalle(detalle.fichaId)}>
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

        {/* Panel Derecho: Liquidación de Pago en Efectivo */}
        <Grid item xs={12} md={4}>
          <Card elevation={3} sx={{ borderRadius: 2 }}>
            <CardContent>
              <Typography variant="h6" fontWeight="bold" gutterBottom>
                {t('settlementTitle')}
              </Typography>
              <Divider sx={{ my: 1.5 }} />

              <Stack spacing={2} sx={{ my: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography variant="body1" color="text.secondary">{t('totalArticles')}</Typography>
                  <Typography variant="body1" fontWeight="bold">
                    {detalles.reduce((sum, l) => sum + Number(l.volumen), 0)}
                  </Typography>
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
                  <Typography variant="h6">{t('totalToPay')}</Typography>
                  <Typography variant="h4" fontWeight="bold" color="text.primary">
                    {formatearMoneda(totalExpendio)}
                  </Typography>
                </Box>

                <Divider />

                <Typography variant="subtitle2" fontWeight="bold">
                  {t('cashPaymentMethod')}
                </Typography>

                <TextField
                  fullWidth
                  label={t('cashReceived')}
                  type="number"
                  value={importeEfectivo}
                  onChange={(e) => setImporteEfectivo(e.target.value)}
                  placeholder="Ej: 50000"
                  helperText={
                    efectivoNum > 0 && efectivoNum < totalExpendio
                      ? `Faltan: ${formatearMoneda(totalExpendio - efectivoNum)}`
                      : ''
                  }
                  error={efectivoNum > 0 && efectivoNum < totalExpendio}
                />

                <Box sx={{ p: 2, bgcolor: 'background.default', borderRadius: 2, textAlign: 'center' }}>
                  <Typography variant="caption" color="text.secondary" display="block">
                    {t('changeToReturn')}
                  </Typography>
                  <Typography variant="h5" fontWeight="bold" color={vuelto > 0 ? 'success.main' : 'text.primary'}>
                    {formatearMoneda(vuelto)}
                  </Typography>
                </Box>

                <Button
                  fullWidth
                  variant="contained"
                  color="secondary"
                  size="large"
                  disabled={loading || detalles.length === 0 || efectivoNum < totalExpendio}
                  onClick={handleRatificarExpendio}
                  sx={{ py: 1.5, fontWeight: 'bold', fontSize: '1.05rem' }}
                >
                  {loading ? <CircularProgress size={26} color="inherit" /> : t('confirmAndDispatch')}
                </Button>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};
