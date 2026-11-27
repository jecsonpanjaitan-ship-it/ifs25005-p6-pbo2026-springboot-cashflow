import { UserConfigFn } from 'vite';
import { overrideVaadinConfig } from './vite.generated.ts';

const customConfig: UserConfigFn = (env) => ({
  // Konfigurasi tambahan Vite dapat ditambahkan di sini.
});

export default overrideVaadinConfig(customConfig);
