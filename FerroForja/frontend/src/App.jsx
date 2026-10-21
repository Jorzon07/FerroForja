import React from 'react';
import { ProveedoresApp } from './nucleo/proveedores/ProveedoresApp';
import { EnrutadorApp } from './nucleo/enrutador/EnrutadorApp';

function App() {
  return (
    <ProveedoresApp>
      <EnrutadorApp />
    </ProveedoresApp>
  );
}

export default App;
