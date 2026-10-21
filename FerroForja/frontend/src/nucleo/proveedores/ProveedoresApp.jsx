import React from 'react';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import theme from '../theme/theme';
import { SesionProvider } from './SesionProvider';
import { IdiomaProvider } from './IdiomaProvider';

export const ProveedoresApp = ({ children }) => {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <IdiomaProvider>
        <SesionProvider>
          {children}
        </SesionProvider>
      </IdiomaProvider>
    </ThemeProvider>
  );
};
