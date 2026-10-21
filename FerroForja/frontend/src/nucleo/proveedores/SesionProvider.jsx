import React, { createContext, useContext, useState, useEffect } from 'react';
import clienteApi, { limpiarTokenCsrf, asegurarTokenCsrf } from '../../servicios/api/clienteApi';

const SesionContext = createContext(null);

export const SesionProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Comprobar la sesión activa en el backend al montar la aplicación
  useEffect(() => {
    const checkActiveSession = async () => {
      try {
        await asegurarTokenCsrf();
        const response = await clienteApi.get('/auth/me');
        setUser(response.data);
      } catch (err) {
        setUser(null);
      } finally {
        setLoading(false);
      }
    };

    checkActiveSession();
  }, []);

  const login = async (username, password) => {
    limpiarTokenCsrf();
    const response = await clienteApi.post('/auth/login', { username, password });
    setUser(response.data);
    await asegurarTokenCsrf();
    return response.data;
  };

  const logout = async () => {
    try {
      await clienteApi.post('/auth/logout');
    } finally {
      limpiarTokenCsrf();
      setUser(null);
      window.location.href = '/login';
    }
  };

  const hasRole = (role) => {
    if (!user || !user.perfiles) return false;
    return user.perfiles.includes(role);
  };

  return (
    <SesionContext.Provider value={{ user, loading, login, logout, hasRole, isAuthenticated: !!user }}>
      {children}
    </SesionContext.Provider>
  );
};

export const useSesion = () => useContext(SesionContext);
