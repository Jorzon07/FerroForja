import React, { useState, useEffect } from 'react';
import {
  Grid,
  Card,
  CardContent,
  Typography,
  Box,
  Button,
  Stack,
  Alert,
  CircularProgress,
} from '@mui/material';
import PointOfSaleIcon from '@mui/icons-material/PointOfSale';
import Inventory2Icon from '@mui/icons-material/Inventory2';
import ShoppingBagIcon from '@mui/icons-material/ShoppingBag';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import StorefrontIcon from '@mui/icons-material/Storefront';
import PeopleIcon from '@mui/icons-material/People';
import { useNavigate } from 'react-router-dom';
import { PALETA } from '../../nucleo/theme/theme';
import { useSesion } from '../../nucleo/proveedores/SesionProvider';
import { useIdioma } from '../../nucleo/proveedores/IdiomaProvider';
import clienteApi from '../../servicios/api/clienteApi';
import { formatearMoneda, formatearVolumen } from '../../utilidades/formatos';

export const TableroPage = () => {
  const { user, hasRole } = useSesion();
  const { t } = useIdioma();
  const navigate = useNavigate();
  const isAdmin = hasRole('ROLE_ADMINISTRADOR');

  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalFichas: 0,
    articulosBajoStock: 0,
    totalExistencias: 0,
  });

  useEffect(() => {
    const fetchDashboardStats = async () => {
      try {
        setLoading(true);
        const [stocksRes, lowStocksRes] = await Promise.all([
          clienteApi.get('/almacen/existencias?size=100'),
          clienteApi.get('/almacen/existencias?existenciaBaja=true&size=100'),
        ]);

        const allStocks = stocksRes.data.content || [];
        const lowStocks = lowStocksRes.data.content || [];

        const totalQty = allStocks.reduce((sum, item) => sum + Number(item.volumenLibre || 0), 0);

        setStats({
          totalFichas: stocksRes.data.totalElements || allStocks.length,
          articulosBajoStock: lowStocksRes.data.totalElements || lowStocks.length,
          totalExistencias: totalQty,
        });
      } catch (err) {
        console.error('Error cargando métricas:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardStats();
  }, []);

  return (
    <Box>
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h3" component="h1">
            {t('dashboardTitle')}
          </Typography>
          <Typography variant="body1" color="text.secondary">
            {t('welcomeUser')}, {user?.nombreVisible} ({isAdmin ? t('admin') : t('operative')})
          </Typography>
        </Box>
        <Button
          variant="contained"
          color="secondary"
          size="large"
          startIcon={<PointOfSaleIcon />}
          onClick={() => navigate('/expendios/nueva')}
          sx={{ py: 1.2, px: 3, fontWeight: 'bold' }}
        >
          {t('newSaleBtn')}
        </Button>
      </Box>

      {stats.articulosBajoStock > 0 && (
        <Alert
          severity="warning"
          icon={<WarningAmberIcon />}
          sx={{ mb: 3 }}
          action={
            <Button color="inherit" size="small" onClick={() => navigate('/almacen?existenciaBaja=true')}>
              {t('viewItems')}
            </Button>
          }
        >
          Hay <strong>{stats.articulosBajoStock} {t('lowStockWarning')}</strong>
        </Alert>
      )}

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid item xs={12} sm={4}>
            <Card sx={{ borderLeft: `10px solid ${PALETA.amarillo}` }}>
              <CardContent sx={{ display: 'flex', alignItems: 'center' }}>
                <Box sx={{ p: 1.5, bgcolor: PALETA.negro, color: PALETA.amarillo, mr: 2, display: 'grid', placeItems: 'center' }}>
                  <StorefrontIcon fontSize="large" />
                </Box>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    {t('kpiCatalog')}
                  </Typography>
                  <Typography variant="h3" className="numero-dato">
                    {stats.totalFichas}
                  </Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} sm={4}>
            <Card sx={{ borderLeft: `10px solid ${PALETA.amarillo}` }}>
              <CardContent sx={{ display: 'flex', alignItems: 'center' }}>
                <Box sx={{ p: 1.5, bgcolor: PALETA.negro, color: PALETA.amarillo, mr: 2, display: 'grid', placeItems: 'center' }}>
                  <Inventory2Icon fontSize="large" />
                </Box>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    {t('kpiStock')}
                  </Typography>
                  <Typography variant="h3" className="numero-dato">
                    {formatearVolumen(stats.totalExistencias)}
                  </Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} sm={4}>
            <Card sx={{ borderLeft: `10px solid ${PALETA.amarillo}` }}>
              <CardContent sx={{ display: 'flex', alignItems: 'center' }}>
                <Box sx={{ p: 1.5, bgcolor: PALETA.negro, color: PALETA.amarillo, mr: 2, display: 'grid', placeItems: 'center' }}>
                  <WarningAmberIcon fontSize="large" />
                </Box>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    {t('kpiCritical')}
                  </Typography>
                  <Typography variant="h3" className="numero-dato" color="error.main">
                    {stats.articulosBajoStock}
                  </Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      <Typography variant="h5" sx={{ mb: 2, display: 'inline-block', borderBottom: `4px solid ${PALETA.amarillo}` }}>
        {t('quickActions')}
      </Typography>

      <Grid container spacing={2}>
        <Grid item xs={12} sm={6} md={3}>
          <Card
            sx={{ p: 2, cursor: 'pointer', transition: 'transform .1s', '&:hover': { bgcolor: PALETA.amarillo, transform: 'translate(-2px,-2px)' } }}
            onClick={() => navigate('/expendios/nueva')}
          >
            <Stack direction="row" spacing={2} alignItems="center">
              <PointOfSaleIcon fontSize="large" />
              <Box>
                <Typography variant="subtitle1" fontWeight="bold">{t('billing')}</Typography>
                <Typography variant="caption" color="text.secondary">{t('billingDesc')}</Typography>
              </Box>
            </Stack>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card
            sx={{ p: 2, cursor: 'pointer', transition: 'transform .1s', '&:hover': { bgcolor: PALETA.amarillo, transform: 'translate(-2px,-2px)' } }}
            onClick={() => navigate('/abastecimientos/nueva')}
          >
            <Stack direction="row" spacing={2} alignItems="center">
              <ShoppingBagIcon fontSize="large" />
              <Box>
                <Typography variant="subtitle1" fontWeight="bold">{t('newPurchase')}</Typography>
                <Typography variant="caption" color="text.secondary">{t('newPurchaseDesc')}</Typography>
              </Box>
            </Stack>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card
            sx={{ p: 2, cursor: 'pointer', transition: 'transform .1s', '&:hover': { bgcolor: PALETA.amarillo, transform: 'translate(-2px,-2px)' } }}
            onClick={() => navigate('/almacen')}
          >
            <Stack direction="row" spacing={2} alignItems="center">
              <Inventory2Icon fontSize="large" />
              <Box>
                <Typography variant="subtitle1" fontWeight="bold">{t('inventoryKardex')}</Typography>
                <Typography variant="caption" color="text.secondary">{t('inventoryKardexDesc')}</Typography>
              </Box>
            </Stack>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card
            sx={{ p: 2, cursor: 'pointer', transition: 'transform .1s', '&:hover': { bgcolor: PALETA.amarillo, transform: 'translate(-2px,-2px)' } }}
            onClick={() => navigate('/socios')}
          >
            <Stack direction="row" spacing={2} alignItems="center">
              <PeopleIcon fontSize="large" />
              <Box>
                <Typography variant="subtitle1" fontWeight="bold">{t('thirdParties')}</Typography>
                <Typography variant="caption" color="text.secondary">{t('thirdPartiesDesc')}</Typography>
              </Box>
            </Stack>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};
