import React, { useState } from 'react';
import {
  Box, Typography, Button, IconButton, Drawer, List, ListItem, ListItemButton,
  ListItemIcon, ListItemText, Chip, ButtonGroup, useMediaQuery,
} from '@mui/material';
import { useTheme } from '@mui/material/styles';
import MenuIcon from '@mui/icons-material/Menu';
import StorefrontIcon from '@mui/icons-material/Storefront';
import PointOfSaleIcon from '@mui/icons-material/PointOfSale';
import Inventory2Icon from '@mui/icons-material/Inventory2';
import ShoppingBagIcon from '@mui/icons-material/ShoppingBag';
import AssignmentReturnIcon from '@mui/icons-material/AssignmentReturn';
import PeopleIcon from '@mui/icons-material/People';
import HistoryIcon from '@mui/icons-material/History';
import SecurityIcon from '@mui/icons-material/Security';
import ManageAccountsIcon from '@mui/icons-material/ManageAccounts';
import InsightsIcon from '@mui/icons-material/Insights';
import LogoutIcon from '@mui/icons-material/Logout';
import DashboardIcon from '@mui/icons-material/Dashboard';
import ConstructionIcon from '@mui/icons-material/Construction';
import { useNavigate, useLocation } from 'react-router-dom';
import { useSesion } from '../../nucleo/proveedores/SesionProvider';
import { useIdioma } from '../../nucleo/proveedores/IdiomaProvider';
import { PALETA } from '../../nucleo/theme/theme';

const ANCHO_RIEL = 272;

export const MarcoPrincipal = ({ children }) => {
  const [menuMovil, setMenuMovil] = useState(false);
  const { user, logout, hasRole } = useSesion();
  const { language, changeLanguage, t } = useIdioma();
  const navigate = useNavigate();
  const location = useLocation();
  const tema = useTheme();
  const esEscritorio = useMediaQuery(tema.breakpoints.up('md'));

  const esAdmin = hasRole('ROLE_ADMINISTRADOR');

  const grupos = [
    {
      titulo: 'Operación',
      items: [
        { text: t('dashboard'), icon: <DashboardIcon />, path: '/dashboard', show: true },
        { text: t('pos'), icon: <PointOfSaleIcon />, path: '/expendios/nueva', show: true },
        { text: t('salesHistory'), icon: <HistoryIcon />, path: '/expendios', show: true },
        { text: t('purchases'), icon: <ShoppingBagIcon />, path: '/abastecimientos/nueva', show: true },
        { text: t('purchasesHistory'), icon: <HistoryIcon />, path: '/abastecimientos', show: true },
        { text: t('returns'), icon: <AssignmentReturnIcon />, path: '/restituciones', show: true },
      ],
    },
    {
      titulo: 'Bodega y catálogo',
      items: [
        { text: t('inventory'), icon: <Inventory2Icon />, path: '/almacen', show: true },
        { text: t('catalog'), icon: <StorefrontIcon />, path: '/catalogo', show: true },
        { text: t('thirdParties'), icon: <PeopleIcon />, path: '/socios', show: true },
      ],
    },
    {
      titulo: 'Administración',
      items: [
        { text: t('observability'), icon: <InsightsIcon />, path: '/observabilidad', show: esAdmin },
        { text: t('audit'), icon: <SecurityIcon />, path: '/bitacora', show: esAdmin },
        { text: t('users'), icon: <ManageAccountsIcon />, path: '/operarios', show: esAdmin },
      ],
    },
  ];

  const irA = (ruta) => {
    navigate(ruta);
    setMenuMovil(false);
  };

  const riel = (
    <Box sx={{ width: ANCHO_RIEL, height: '100%', bgcolor: PALETA.negro, color: '#fff', display: 'flex', flexDirection: 'column' }}>
      <Box className="franja-peligro-fina" />
      <Box sx={{ px: 2.5, py: 2.5, display: 'flex', alignItems: 'center', gap: 1.5, borderBottom: `3px solid ${PALETA.amarillo}` }}>
        <Box sx={{ width: 44, height: 44, bgcolor: PALETA.amarillo, color: PALETA.negro, display: 'grid', placeItems: 'center', border: '2px solid #fff' }}>
          <ConstructionIcon />
        </Box>
        <Box>
          <Typography variant="h5" component="div" sx={{ color: PALETA.amarillo, lineHeight: 1, fontSize: '1.9rem' }}>
            {t('appName')}
          </Typography>
          <Typography variant="overline" sx={{ color: 'rgba(255,255,255,0.7)', lineHeight: 1.2 }}>
            Control de bodega
          </Typography>
        </Box>
      </Box>

      <Box sx={{ flexGrow: 1, overflowY: 'auto', py: 1 }}>
        {grupos.map((g) => {
          const visibles = g.items.filter((i) => i.show);
          if (!visibles.length) return null;
          return (
            <Box key={g.titulo} sx={{ mb: 1 }}>
              <Typography variant="overline" sx={{ px: 2.5, color: PALETA.amarillo, opacity: 0.85 }}>
                {g.titulo}
              </Typography>
              <List dense disablePadding sx={{ px: 1.5 }}>
                {visibles.map((item) => {
                  const activo = location.pathname === item.path;
                  return (
                    <ListItem key={item.path} disablePadding sx={{ mb: 0.5 }}>
                      <ListItemButton
                        selected={activo}
                        onClick={() => irA(item.path)}
                        sx={{
                          color: activo ? PALETA.negro : '#fff',
                          border: '2px solid transparent',
                          '&:hover': { bgcolor: 'rgba(255,196,0,0.18)', borderColor: PALETA.amarillo },
                        }}
                      >
                        <ListItemIcon sx={{ color: 'inherit', minWidth: 38 }}>{item.icon}</ListItemIcon>
                        <ListItemText
                          primary={item.text}
                          primaryTypographyProps={{ sx: { fontFamily: '"Barlow Condensed", sans-serif', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em', fontSize: '0.95rem' } }}
                        />
                      </ListItemButton>
                    </ListItem>
                  );
                })}
              </List>
            </Box>
          );
        })}
      </Box>

      {user && (
        <Box sx={{ p: 2, borderTop: '2px solid rgba(255,255,255,0.2)' }}>
          <Chip
            size="small"
            label={esAdmin ? t('admin') : t('operative')}
            sx={{ bgcolor: PALETA.amarillo, color: PALETA.negro, borderColor: PALETA.amarillo, mb: 1 }}
          />
          <Typography variant="body2" sx={{ fontWeight: 600, mb: 1.5 }} noWrap>
            {user.nombreVisible}
          </Typography>
          <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
            <ButtonGroup size="small" variant="outlined">
              {['es', 'en'].map((idioma) => (
                <Button
                  key={idioma}
                  onClick={() => changeLanguage(idioma)}
                  sx={{
                    minWidth: 38, px: 1, boxShadow: 'none !important', borderColor: '#fff !important',
                    color: language === idioma ? PALETA.negro : '#fff',
                    bgcolor: language === idioma ? PALETA.amarillo : 'transparent',
                    '&:hover': { bgcolor: language === idioma ? PALETA.amarillo : 'rgba(255,255,255,0.12)' },
                  }}
                >
                  {idioma.toUpperCase()}
                </Button>
              ))}
            </ButtonGroup>
            <Button
              onClick={logout}
              startIcon={<LogoutIcon />}
              size="small"
              sx={{ ml: 'auto', color: '#fff', border: '2px solid #fff', '&:hover': { bgcolor: '#fff', color: PALETA.negro } }}
            >
              {t('logout')}
            </Button>
          </Box>
        </Box>
      )}
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      {esEscritorio ? (
        <Box component="nav" className="no-imprimir" sx={{ width: ANCHO_RIEL, flexShrink: 0, position: 'sticky', top: 0, height: '100vh', alignSelf: 'flex-start' }}>
          {riel}
        </Box>
      ) : (
        <Drawer open={menuMovil} onClose={() => setMenuMovil(false)} PaperProps={{ sx: { borderRadius: 0 } }}>
          {riel}
        </Drawer>
      )}

      <Box sx={{ flexGrow: 1, minWidth: 0, display: 'flex', flexDirection: 'column' }}>
        {!esEscritorio && (
          <Box className="no-imprimir" sx={{ bgcolor: PALETA.negro, color: PALETA.amarillo, display: 'flex', alignItems: 'center', px: 1, py: 0.5, borderBottom: `4px solid ${PALETA.amarillo}`, position: 'sticky', top: 0, zIndex: 10 }}>
            <IconButton color="inherit" onClick={() => setMenuMovil(true)} aria-label="Abrir menú">
              <MenuIcon />
            </IconButton>
            <Typography variant="h5" component="div" sx={{ ml: 1 }}>{t('appName')}</Typography>
          </Box>
        )}

        <Box component="main" sx={{ flexGrow: 1, p: { xs: 2, md: 4 } }}>
          {children}
        </Box>

        <Box component="footer" className="no-imprimir" sx={{ bgcolor: PALETA.negro, color: 'rgba(255,255,255,0.75)', px: 3, py: 1.2, display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', gap: 1 }}>
          <Typography variant="caption" sx={{ fontFamily: '"JetBrains Mono", monospace' }}>
            Control de bodega · Moneda COP · Sede única (demostración académica)
          </Typography>
          <Typography variant="caption" sx={{ color: PALETA.amarillo, fontFamily: '"JetBrains Mono", monospace' }}>
            v2.0
          </Typography>
        </Box>
      </Box>
    </Box>
  );
};
