import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useSesion } from '../proveedores/SesionProvider';
import { MarcoPrincipal } from '../../componentes/maquetacion/MarcoPrincipal';
import { AccesoPage } from '../../modulos/acceso/AccesoPage';
import { TableroPage } from '../../modulos/tablero/TableroPage';
import { CatalogoPage } from '../../modulos/catalogo/CatalogoPage';
import { SociosPage } from '../../modulos/socios/SociosPage';
import { AlmacenPage } from '../../modulos/almacen/AlmacenPage';
import { MostradorPage } from '../../modulos/expendios/MostradorPage';
import { HistorialExpendiosPage } from '../../modulos/expendios/HistorialExpendiosPage';
import { RecepcionNuevaPage } from '../../modulos/abastecimientos/RecepcionNuevaPage';
import { HistorialAbastecimientosPage } from '../../modulos/abastecimientos/HistorialAbastecimientosPage';
import { RestitucionesPage } from '../../modulos/restituciones/RestitucionesPage';
import { TiqueteImpresionPage } from '../../modulos/folios/TiqueteImpresionPage';
import { BitacoraPage } from '../../modulos/bitacora/BitacoraPage';
import { OperariosPage } from '../../modulos/operarios/OperariosPage';
import { PanelObservabilidad } from '../../modulos/reportes/PanelObservabilidad';
import { Box, CircularProgress } from '@mui/material';

const ProtectedRoute = ({ children, requiredRole }) => {
  const { user, loading, hasRole } = useSesion();

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole && !hasRole(requiredRole)) {
    return <Navigate to="/dashboard" replace />;
  }

  return <MarcoPrincipal>{children}</MarcoPrincipal>;
};

export const EnrutadorApp = () => {
  const { user, loading } = useSesion();

  return (
    <BrowserRouter>
      <Routes>
        {/* Ruta pública de login */}
        <Route
          path="/login"
          element={!loading && user ? <Navigate to="/dashboard" replace /> : <AccesoPage />}
        />

        {/* Rutas protegidas para todos los usuarios autenticados */}
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <TableroPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <TableroPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/expendios/nueva"
          element={
            <ProtectedRoute>
              <MostradorPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/expendios"
          element={
            <ProtectedRoute>
              <HistorialExpendiosPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/abastecimientos/nueva"
          element={
            <ProtectedRoute>
              <RecepcionNuevaPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/abastecimientos"
          element={
            <ProtectedRoute>
              <HistorialAbastecimientosPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/almacen"
          element={
            <ProtectedRoute>
              <AlmacenPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/catalogo"
          element={
            <ProtectedRoute>
              <CatalogoPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/socios"
          element={
            <ProtectedRoute>
              <SociosPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/restituciones"
          element={
            <ProtectedRoute>
              <RestitucionesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/tiquetes/:tipo/:correlativo"
          element={
            <ProtectedRoute>
              <TiqueteImpresionPage />
            </ProtectedRoute>
          }
        />

        {/* Rutas exclusivas para ADMINISTRADOR */}
        <Route
          path="/observabilidad"
          element={
            <ProtectedRoute requiredRole="ROLE_ADMINISTRADOR">
              <PanelObservabilidad />
            </ProtectedRoute>
          }
        />
        <Route
          path="/bitacora"
          element={
            <ProtectedRoute requiredRole="ROLE_ADMINISTRADOR">
              <BitacoraPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/operarios"
          element={
            <ProtectedRoute requiredRole="ROLE_ADMINISTRADOR">
              <OperariosPage />
            </ProtectedRoute>
          }
        />

        {/* Ruta por defecto */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
};
