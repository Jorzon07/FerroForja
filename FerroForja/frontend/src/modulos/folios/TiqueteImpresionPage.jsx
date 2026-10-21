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
  Button,
  Divider,
  CircularProgress,
  Alert,
  Stack,
  Container,
  Grid,
} from '@mui/material';
import PrintIcon from '@mui/icons-material/Print';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { useParams, useNavigate } from 'react-router-dom';
import clienteApi from '../../servicios/api/clienteApi';
import { formatearMoneda, formatearVolumen, formatearFechaHora } from '../../utilidades/formatos';

/**
 * Vista de Comprobante Imprimible:
 * Diseñada para simular un ticket / factura de mostrador de una ferretería en Colombia (RN-12).
 * Formateada con estilos CSS limpios para impresión directa mediante window.print().
 */
export const TiqueteImpresionPage = () => {
  const { tipo, correlativo } = useParams();
  const navigate = useNavigate();

  const [folio, setFolio] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchTiquete = async () => {
      try {
        setLoading(true);
        setError(null);
        const response = await clienteApi.get(`/folios/${tipo}/${correlativo}`);
        setFolio(response.data);
      } catch (err) {
        setError(err.response?.data?.detail || 'No se pudo cargar el comprobante solicitado');
      } finally {
        setLoading(false);
      }
    };

    fetchTiquete();
  }, [tipo, correlativo]);

  const handlePrint = () => {
    window.print();
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 8 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error || !folio) {
    return (
      <Container maxWidth="sm" sx={{ py: 6 }}>
        <Alert severity="error" sx={{ mb: 2 }}>{error || 'Comprobante no encontrado'}</Alert>
        <Button variant="outlined" startIcon={<ArrowBackIcon />} onClick={() => navigate(-1)}>
          Volver
        </Button>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ py: 3 }}>
      {/* Botones de acción (ocultos en la impresión mediante CSS @media print) */}
      <Box
        sx={{
          mb: 3,
          display: 'flex',
          justifyContent: 'space-between',
          '@media print': { display: 'none' },
        }}
      >
        <Button variant="outlined" startIcon={<ArrowBackIcon />} onClick={() => navigate(-1)}>
          Volver a Historial
        </Button>
        <Button variant="contained" startIcon={<PrintIcon />} onClick={handlePrint} size="large">
          Imprimir Comprobante
        </Button>
      </Box>

      {/* Recibo estructurado */}
      <Paper
        elevation={3}
        sx={{
          p: 4,
          borderRadius: 2,
          bgcolor: 'background.paper',
          border: '1px solid #cbd5e1',
          '@media print': {
            boxShadow: 'none',
            border: 'none',
            p: 1,
          },
        }}
      >
        {/* Encabezado Ferretería */}
        <Box sx={{ textAlign: 'center', mb: 3 }}>
          <Typography variant="h5" fontWeight="bold">
            {folio.emisorDenominacion}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            NIT: {folio.emisorNit} — Régimen Simplificado (Demostración Académica)
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {folio.emisorUbicacion} | Tel: {folio.emisorCelular}
          </Typography>
          <Typography variant="caption" sx={{ display: 'block', mt: 0.5, fontStyle: 'italic', color: 'text.secondary' }}>
            * Facturación Electrónica Simulada — Sin integración real con la DIAN (RN-12) *
          </Typography>
        </Box>

        <Divider sx={{ my: 2 }} />

        {/* Datos del Documento y Tercero */}
        <Grid container spacing={2} sx={{ mb: 3 }}>
          <Grid item xs={12} sm={6}>
            <Typography variant="subtitle2" color="text.secondary">COMPROBANTE INTERNO:</Typography>
            <Typography variant="h6" fontWeight="bold" sx={{ fontFamily: 'monospace' }}>
              {folio.tipoIdentificacion} #{folio.correlativo}
            </Typography>
            <Typography variant="body2">
              <strong>Fecha / Hora:</strong> {formatearFechaHora(folio.marcaTiempo)}
            </Typography>
            <Typography variant="body2">
              <strong>Estado:</strong> {folio.situacion}
            </Typography>
            <Typography variant="body2">
              <strong>Atendido por:</strong> {folio.operarioResponsable}
            </Typography>
          </Grid>

          <Grid item xs={12} sm={6}>
            <Typography variant="subtitle2" color="text.secondary">
              {folio.tipoIdentificacion === 'ABASTECIMIENTO' ? 'DATOS DEL PROVEEDOR:' : 'DATOS DEL CLIENTE:'}
            </Typography>
            <Typography variant="body1" fontWeight="bold">
              {folio.socioDenominacion || 'Consumidor Final'}
            </Typography>
            <Typography variant="body2">
              <strong>Identificación:</strong> {folio.socioFolio || '222222222222'}
            </Typography>
            {folio.socioUbicacion && (
              <Typography variant="body2"><strong>Dirección:</strong> {folio.socioUbicacion}</Typography>
            )}
            {folio.socioCelular && (
              <Typography variant="body2"><strong>Teléfono:</strong> {folio.socioCelular}</Typography>
            )}
          </Grid>
        </Grid>

        {folio.causa && (
          <Alert severity="warning" sx={{ mb: 2 }}>
            <strong>Motivo Documentado:</strong> {folio.causa}
          </Alert>
        )}

        {/* Tabla de Renglones de Comprobante */}
        <TableContainer sx={{ mb: 3 }}>
          <Table size="small">
            <TableHead sx={{ bgcolor: 'action.hover' }}>
              <TableRow>
                <TableCell><strong>Cód.</strong></TableCell>
                <TableCell><strong>Descripción del Artículo</strong></TableCell>
                <TableCell align="center"><strong>Cant.</strong></TableCell>
                <TableCell align="right"><strong>Precio Unit.</strong></TableCell>
                <TableCell align="right"><strong>Subtotal (COP)</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {folio.detalles?.map((l, index) => (
                <TableRow key={index}>
                  <TableCell sx={{ fontFamily: 'monospace' }}>{l.codigoEan}</TableCell>
                  <TableCell>{l.resena}</TableCell>
                  <TableCell align="center">{formatearVolumen(l.volumen)} {l.medida}</TableCell>
                  <TableCell align="right">{formatearMoneda(l.valorIndividual)}</TableCell>
                  <TableCell align="right" sx={{ fontWeight: 'bold' }}>{formatearMoneda(l.subtotal)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>

        {/* Totales y Liquidación en Efectivo */}
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 3 }}>
          <Box sx={{ width: 280 }}>
            <Stack spacing={1}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="body2">Subtotal:</Typography>
                <Typography variant="body2" fontWeight="bold">{formatearMoneda(folio.subtotal || folio.total)}</Typography>
              </Box>
              <Divider />
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
                <Typography variant="h6" fontWeight="bold">TOTAL COP:</Typography>
                <Typography variant="h5" fontWeight="bold" color="text.primary">
                  {formatearMoneda(folio.total)}
                </Typography>
              </Box>

              {folio.importeAbonado && (
                <>
                  <Divider />
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="body2">Efectivo Recibido:</Typography>
                    <Typography variant="body2">{formatearMoneda(folio.importeAbonado)}</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="body2" fontWeight="bold">Cambio Entregado:</Typography>
                    <Typography variant="body2" fontWeight="bold">{formatearMoneda(folio.vuelto)}</Typography>
                  </Box>
                </>
              )}
            </Stack>
          </Box>
        </Box>

        {/* Pie de Recibo */}
        <Box sx={{ textAlign: 'center', mt: 4, pt: 2, borderTop: '1px dashed #94a3b8' }}>
          <Typography variant="caption" display="block">
            ¡Gracias por su compra en FerroForja!
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Conserve este comprobante para cualquier trámite o devolución de mercancía.
          </Typography>
        </Box>
      </Paper>
    </Container>
  );
};
