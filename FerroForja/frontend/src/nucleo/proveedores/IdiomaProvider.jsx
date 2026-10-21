import React, { createContext, useContext, useState, useEffect } from 'react';
import { diccionarios } from '../../utilidades/textos';

const IdiomaContext = createContext();

export const IdiomaProvider = ({ children }) => {
  const [language, setLanguage] = useState(() => {
    return localStorage.getItem('app_language') || 'es';
  });

  const changeLanguage = (lang) => {
    if (diccionarios[lang]) {
      setLanguage(lang);
      localStorage.setItem('app_language', lang);
    }
  };

  const t = (key) => {
    return diccionarios[language]?.[key] || diccionarios['es']?.[key] || key;
  };

  return (
    <IdiomaContext.Provider value={{ language, changeLanguage, t }}>
      {children}
    </IdiomaContext.Provider>
  );
};

export const useIdioma = () => useContext(IdiomaContext);
