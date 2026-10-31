import logoPuriy from '../../../compartido/marca/puriy-logo-dark.svg'
import { EscenaDelAmanecer } from './EscenaDelAmanecer'

export function PantallaDeBienvenida() {
  return (
    <div className="flex min-h-dvh flex-col overflow-hidden bg-noche font-sans text-arena antialiased">
      <header className="flex justify-center px-6 pt-10 sm:pt-14">
        <img src={logoPuriy} alt="Puriy" width={132} height={40} className="h-auto w-28 sm:w-33" />
      </header>

      <main className="mx-auto flex w-full max-w-3xl flex-col items-center px-6 pt-12 pb-10 text-center sm:pt-16">
        <p className="flex items-center gap-3 text-xs font-medium tracking-[0.23em] text-neblina">
          <span aria-hidden="true" className="size-1.5 rounded-full bg-amanecer" />
          PRÓXIMAMENTE
        </p>
        <h1 className="mt-6 text-4xl leading-[1.12] font-medium tracking-[-0.05em] text-balance sm:text-6xl">
          Muy pronto estaremos <span className="text-amanecer">disponibles.</span>
        </h1>
        <p className="mt-6 max-w-lg text-base leading-relaxed text-pretty text-neblina">
          Estamos preparando todo para que compres tu pasaje de bus en línea. Viaja de noche y
          despierta en tu destino.
        </p>
      </main>

      <EscenaDelAmanecer />

      <footer className="bg-noche px-6 py-6 text-center text-xs text-neblina">
        Puriy · Viaja a tu manera
      </footer>
    </div>
  )
}
