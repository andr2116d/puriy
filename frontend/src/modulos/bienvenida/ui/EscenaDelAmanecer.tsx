export function EscenaDelAmanecer() {
  return (
    <svg
      aria-hidden="true"
      viewBox="200 0 400 300"
      preserveAspectRatio="xMidYMax meet"
      className="mt-auto block h-[clamp(200px,38dvh,340px)] w-full shrink-0 overflow-visible"
    >
      <clipPath id="sobre-el-horizonte">
        <rect x="200" width="400" height="150" />
      </clipPath>
      <g clipPath="url(#sobre-el-horizonte)">
        <circle
          cx="400"
          cy="150"
          r="118"
          className="animate-amanecer fill-amanecer motion-reduce:animate-none"
        />
      </g>
      <rect x="-4000" y="150" width="8800" height="5" className="fill-arena" />
      <path d="M385 170 H415 L560 300 H240 Z" className="fill-ruta" />
      <path
        d="M397 182 h6 l1 16 h-8 Z M394 214 h12 l2 24 h-16 Z M390 258 h20 l3 42 h-26 Z"
        className="fill-arena"
      />
    </svg>
  )
}
