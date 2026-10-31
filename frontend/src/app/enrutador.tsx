import {
  Navigate,
  createRootRoute,
  createRoute,
  createRouter,
  type RouterHistory,
} from '@tanstack/react-router'
import { PantallaDeBienvenida } from '../modulos/bienvenida/ui/PantallaDeBienvenida'

const rutaRaiz = createRootRoute({
  notFoundComponent: () => <Navigate to="/" replace />,
})

const rutaInicio = createRoute({
  getParentRoute: () => rutaRaiz,
  path: '/',
  component: PantallaDeBienvenida,
})

export function crearEnrutador(history?: RouterHistory) {
  return createRouter({ routeTree: rutaRaiz.addChildren([rutaInicio]), history })
}

declare module '@tanstack/react-router' {
  interface Register {
    router: ReturnType<typeof crearEnrutador>
  }
}
