import React, { useState } from 'react';
import {
  Box,
  Typography,
  TextField,
  Button,
  Alert,
  CircularProgress,
} from '@mui/material';
import ConstructionIcon from '@mui/icons-material/Construction';
import { PALETA } from '../../nucleo/theme/theme';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { useSesion } from '../../nucleo/proveedores/SesionProvider';
import { useNavigate, useLocation } from 'react-router-dom';

const validationSchema = Yup.object({
  username: Yup.string().required('El usuario es obligatorio'),
  password: Yup.string().required('La contraseña es obligatoria'),
});

export const AccesoPage = () => {
  const { login } = useSesion();
  const navigate = useNavigate();
  const location = useLocation();
  const [errorMessage, setErrorMessage] = useState(null);

  const queryParams = new URLSearchParams(location.search);
  const isExpired = queryParams.get('expired') === 'true';

  const formik = useFormik({
    initialValues: {
      username: '',
      password: '',
    },
    validationSchema,
    onSubmit: async (values, { setSubmitting }) => {
      setErrorMessage(null);
      try {
        await login(values.username, values.password);
        navigate('/dashboard');
      } catch (err) {
        if (err.response && err.response.data && err.response.data.detail) {
          setErrorMessage(err.response.data.detail);
        } else if (err.response && err.response.status === 401) {
          setErrorMessage('Nombre de usuario o contraseña incorrectos');
        } else {
          setErrorMessage('No fue posible conectar con el servidor. Verifique que el backend esté activo.');
        }
      } finally {
        setSubmitting(false);
      }
    },
  });

  return (
    <Box sx={{ minHeight: '100vh', display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1.15fr 1fr' } }}>
      {/* Panel de marca */}
      <Box
        sx={{
          bgcolor: PALETA.negro, color: '#fff', p: { xs: 4, md: 8 }, display: 'flex', flexDirection: 'column',
          justifyContent: 'space-between', position: 'relative', overflow: 'hidden', minHeight: { xs: 260, md: 'auto' },
        }}
      >
        <Box className="franja-peligro" sx={{ position: 'absolute', top: 0, left: 0, right: 0, height: 18 }} />
        <Box sx={{ mt: 4 }}>
          <Box sx={{ display: 'inline-grid', placeItems: 'center', width: 72, height: 72, bgcolor: PALETA.amarillo, color: PALETA.negro, border: '3px solid #fff', mb: 3 }}>
            <ConstructionIcon sx={{ fontSize: 44 }} />
          </Box>
          <Typography variant="h1" component="h1" sx={{ fontSize: { xs: '3.6rem', md: '6.5rem' }, lineHeight: 0.9, color: PALETA.amarillo }}>
            Ferro<br />Forja
          </Typography>
          <Typography variant="h6" sx={{ mt: 2, color: 'rgba(255,255,255,0.85)' }}>
            Control de bodega · Kárdex · Mostrador
          </Typography>
        </Box>
        <Typography variant="caption" sx={{ display: { xs: 'none', md: 'block' }, fontFamily: '"JetBrains Mono", monospace', opacity: 0.6 }}>
          Existencias exactas, costo medio ponderado y trazabilidad por cada asiento.
        </Typography>
      </Box>

      {/* Formulario */}
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', p: { xs: 3, md: 6 } }}>
        <Box sx={{ width: '100%', maxWidth: 420 }}>
          <Typography variant="overline" sx={{ color: PALETA.acero }}>
            Acceso restringido
          </Typography>
          <Typography variant="h4" component="h2" gutterBottom>
            Iniciar sesión
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            Sistema de Gestión de Inventario
          </Typography>

          {isExpired && (
            <Alert severity="warning" sx={{ mb: 2 }}>
              Su sesión ha expirado por inactividad. Ingrese nuevamente.
            </Alert>
          )}

          {errorMessage && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {errorMessage}
            </Alert>
          )}

          <form onSubmit={formik.handleSubmit}>
            <TextField
              fullWidth
              id="username"
              name="username"
              label="Nombre de Usuario"
              variant="outlined"
              margin="normal"
              value={formik.values.username}
              onChange={formik.handleChange}
              onBlur={formik.handleBlur}
              error={formik.touched.username && Boolean(formik.errors.username)}
              helperText={formik.touched.username && formik.errors.username}
              autoFocus
            />

            <TextField
              fullWidth
              id="password"
              name="password"
              label="Contraseña"
              type="password"
              variant="outlined"
              margin="normal"
              value={formik.values.password}
              onChange={formik.handleChange}
              onBlur={formik.handleBlur}
              error={formik.touched.password && Boolean(formik.errors.password)}
              helperText={formik.touched.password && formik.errors.password}
            />

            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              startIcon={<LockOutlinedIcon />}
              disabled={formik.isSubmitting}
              sx={{ mt: 3, py: 1.4, fontSize: '1.15rem' }}
            >
              {formik.isSubmitting ? <CircularProgress size={24} color="inherit" /> : 'Entrar al Sistema'}
            </Button>
          </form>
        </Box>
      </Box>
    </Box>
  );
};
