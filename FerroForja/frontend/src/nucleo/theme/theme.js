import { createTheme, alpha } from '@mui/material/styles';

/**
 * Tema industrial FerroForja.
 * Identidad: amarillo de seguridad (#FFC400) sobre negro de taller (#141414),
 * esquinas casi rectas, bordes gruesos, sombras "duras" y tipografía condensada.
 */
export const PALETA = {
  amarillo: '#FFC400',
  amarilloOscuro: '#C79500',
  amarilloSuave: '#FFF3C4',
  negro: '#141414',
  grafito: '#262626',
  acero: '#5B5B5B',
  concreto: '#ECEAE4',
  blanco: '#FFFFFF',
};

const BORDE = `2px solid ${PALETA.negro}`;
const SOMBRA_DURA = `4px 4px 0 ${PALETA.negro}`;
const FUENTE_TITULOS = '"Barlow Condensed", "Arial Narrow", Impact, sans-serif';
const FUENTE_TEXTO = '"Barlow", "Helvetica Neue", Arial, sans-serif';
const FUENTE_DATOS = '"JetBrains Mono", "Consolas", monospace';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: PALETA.amarillo, light: '#FFD84D', dark: PALETA.amarilloOscuro, contrastText: PALETA.negro },
    secondary: { main: PALETA.negro, light: PALETA.grafito, dark: '#000000', contrastText: PALETA.amarillo },
    error: { main: '#C62828', light: '#E57373', dark: '#8E0000', contrastText: '#fff' },
    warning: { main: '#EF6C00', light: '#FFB74D', dark: '#B53D00', contrastText: PALETA.negro },
    success: { main: '#2E7D32', light: '#81C784', dark: '#1B5E20', contrastText: '#fff' },
    info: { main: '#01579B', light: '#4FC3F7', dark: '#002F6C', contrastText: '#fff' },
    background: { default: PALETA.concreto, paper: PALETA.blanco },
    text: { primary: PALETA.negro, secondary: PALETA.acero },
    divider: 'rgba(20,20,20,0.18)',
    action: { hover: alpha(PALETA.amarillo, 0.16), selected: alpha(PALETA.amarillo, 0.3) },
  },
  shape: { borderRadius: 2 },
  typography: {
    fontFamily: FUENTE_TEXTO,
    h1: { fontFamily: FUENTE_TITULOS, fontWeight: 800, textTransform: 'uppercase', letterSpacing: '0.02em' },
    h2: { fontFamily: FUENTE_TITULOS, fontWeight: 800, textTransform: 'uppercase', letterSpacing: '0.02em' },
    h3: { fontFamily: FUENTE_TITULOS, fontWeight: 800, textTransform: 'uppercase', letterSpacing: '0.02em' },
    h4: { fontFamily: FUENTE_TITULOS, fontWeight: 800, textTransform: 'uppercase', letterSpacing: '0.03em' },
    h5: { fontFamily: FUENTE_TITULOS, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.03em' },
    h6: { fontFamily: FUENTE_TITULOS, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.04em' },
    subtitle1: { fontWeight: 600 },
    button: { fontFamily: FUENTE_TITULOS, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.06em', fontSize: '1rem' },
    overline: { fontFamily: FUENTE_TITULOS, fontWeight: 700, letterSpacing: '0.12em' },
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: { backgroundColor: PALETA.concreto, backgroundImage: 'radial-gradient(rgba(20,20,20,0.07) 1px, transparent 1px)', backgroundSize: '18px 18px' },
        'code, .datos': { fontFamily: FUENTE_DATOS },
        '::selection': { background: PALETA.amarillo, color: PALETA.negro },
      },
    },
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: {
        root: { borderRadius: 2, border: BORDE, paddingInline: 18, transition: 'transform .08s, box-shadow .08s' },
        contained: {
          boxShadow: '3px 3px 0 rgba(20,20,20,1)',
          '&:hover': { transform: 'translate(-1px,-1px)', boxShadow: '5px 5px 0 rgba(20,20,20,1)' },
          '&:active': { transform: 'translate(2px,2px)', boxShadow: '1px 1px 0 rgba(20,20,20,1)' },
          '&.Mui-disabled': { border: '2px solid rgba(20,20,20,0.2)', boxShadow: 'none' },
        },
        containedSecondary: { '&:hover': { backgroundColor: PALETA.grafito } },
        outlined: { borderColor: PALETA.negro, color: PALETA.negro, '&:hover': { backgroundColor: PALETA.amarilloSuave, borderColor: PALETA.negro } },
        text: { border: '2px solid transparent', '&:hover': { backgroundColor: alpha(PALETA.amarillo, 0.25) } },
      },
    },
    MuiIconButton: { styleOverrides: { root: { borderRadius: 2 } } },
    MuiPaper: {
      styleOverrides: {
        root: { borderRadius: 2, backgroundImage: 'none' },
        outlined: { border: BORDE },
        elevation1: { border: BORDE, boxShadow: SOMBRA_DURA },
        elevation2: { border: BORDE, boxShadow: SOMBRA_DURA },
        elevation3: { border: BORDE, boxShadow: SOMBRA_DURA },
        elevation4: { border: BORDE, boxShadow: SOMBRA_DURA },
      },
    },
    MuiCard: { styleOverrides: { root: { border: BORDE, boxShadow: SOMBRA_DURA, borderRadius: 2 } } },
    MuiTableContainer: { styleOverrides: { root: { border: BORDE, boxShadow: SOMBRA_DURA } } },
    MuiTableHead: {
      styleOverrides: {
        root: {
          backgroundColor: `${PALETA.negro} !important`,
          '& .MuiTableCell-root': {
            backgroundColor: PALETA.negro, color: PALETA.amarillo, fontFamily: FUENTE_TITULOS,
            fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.08em', fontSize: '0.95rem', borderBottom: `3px solid ${PALETA.amarillo}`,
          },
        },
      },
    },
    MuiTableRow: { styleOverrides: { root: { '&:nth-of-type(even) td': { backgroundColor: 'rgba(20,20,20,0.025)' }, '&.MuiTableRow-hover:hover': { backgroundColor: alpha(PALETA.amarillo, 0.22) } } } },
    MuiTableCell: { styleOverrides: { root: { borderBottom: '1px solid rgba(20,20,20,0.15)' } } },
    MuiChip: { styleOverrides: { root: { borderRadius: 2, fontFamily: FUENTE_TITULOS, fontWeight: 700, letterSpacing: '0.06em', border: '1.5px solid ' + PALETA.negro } } },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          borderRadius: 2, backgroundColor: PALETA.blanco,
          '& .MuiOutlinedInput-notchedOutline': { borderWidth: 2, borderColor: PALETA.negro },
          '&:hover .MuiOutlinedInput-notchedOutline': { borderColor: PALETA.negro },
          '&.Mui-focused .MuiOutlinedInput-notchedOutline': { borderColor: PALETA.negro, borderWidth: 3, boxShadow: `3px 3px 0 ${PALETA.amarillo}` },
        },
      },
    },
    MuiInputLabel: { styleOverrides: { root: { fontWeight: 600, '&.Mui-focused': { color: PALETA.negro } } } },
    MuiDialog: { styleOverrides: { paper: { border: BORDE, boxShadow: `8px 8px 0 ${PALETA.negro}`, borderRadius: 2 } } },
    MuiDialogTitle: { styleOverrides: { root: { fontFamily: FUENTE_TITULOS, fontWeight: 800, textTransform: 'uppercase', backgroundColor: PALETA.amarillo, borderBottom: BORDE } } },
    MuiAlert: {
      styleOverrides: {
        root: { borderRadius: 2, border: BORDE, fontWeight: 500 },
        standardWarning: { backgroundColor: PALETA.amarilloSuave, color: PALETA.negro },
      },
    },
    MuiTooltip: { styleOverrides: { tooltip: { backgroundColor: PALETA.negro, color: PALETA.amarillo, borderRadius: 2, fontWeight: 600 } } },
    MuiListItemButton: {
      styleOverrides: { root: { borderRadius: 2, '&.Mui-selected': { backgroundColor: PALETA.amarillo, color: PALETA.negro, '&:hover': { backgroundColor: '#FFD84D' } } } },
    },
  },
});

export default theme;
