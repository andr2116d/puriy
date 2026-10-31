import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { RouterProvider } from '@tanstack/react-router'
import { crearEnrutador } from './enrutador'

const clienteDeConsultas = new QueryClient()
const enrutador = crearEnrutador()

export function Aplicacion() {
  return (
    <QueryClientProvider client={clienteDeConsultas}>
      <RouterProvider router={enrutador} />
    </QueryClientProvider>
  )
}
