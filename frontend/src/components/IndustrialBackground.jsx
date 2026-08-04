// Zontano viomixaniko fonto gia tin othoni syndesis.
// Einai inline SVG (oxi eikona) oste na mporoume na to kinisoume me CSS:
// grannazia pou gyrizoun, elastro me lamarina aloyminiou, xytirio me liomeno
// metallo, kapnos apo kaminades kai roi mesa stous solines.
//
// SYNTHESI: ola ta stoixeia einai topothetimena stis AKRES (aristera, dexia,
// pano, kato). To KENTRO tou kadrou meneI SKOPIMA ADEIO, giati ekei kathetai
// i forma syndesis - etsi den kryvetai tipota apo ta animation.
//
// Ola ta animation einai transform/opacity -> ta analambanei i karta grafikon,
// opote den varainoun ton epexergasti. Sevomaste kai to "reduce motion".

function Gear({ cx, cy, rOut, rIn, teeth, toothW, toothH, sw, opacity, dur, dir = 1 }) {
  return (
    <g transform={`translate(${cx},${cy})`}>
      <g className={dir > 0 ? "mt-gear-cw" : "mt-gear-ccw"} style={{ animationDuration: `${dur}s` }}>
        <g fill="none" stroke="#dbeafe" strokeOpacity={opacity} strokeWidth={sw} strokeLinejoin="round">
          <circle r={rOut} />
          <circle r={rIn} />
          {Array.from({ length: teeth }).map((_, i) => (
            <rect
              key={i}
              x={-toothW / 2}
              y={-(rOut + toothH)}
              width={toothW}
              height={toothH + sw}
              rx="2"
              transform={`rotate(${(360 / teeth) * i})`}
            />
          ))}
          {Array.from({ length: 6 }).map((_, i) => {
            const a = (Math.PI / 3) * i;
            return (
              <line
                key={i}
                x1={rIn * Math.cos(a)}
                y1={rIn * Math.sin(a)}
                x2={(rOut - 4) * Math.cos(a)}
                y2={(rOut - 4) * Math.sin(a)}
              />
            );
          })}
        </g>
      </g>
    </g>
  );
}

// Kylindros elasis: gyrizei gyro apo ton eauto tou
function Roller({ cx, cy, r, dur, dir = 1 }) {
  return (
    <g transform={`translate(${cx},${cy})`}>
      <circle r={r} fill="none" stroke="#bfdbfe" strokeOpacity="0.34" strokeWidth="3.4" />
      <circle r={r * 0.28} fill="none" stroke="#bfdbfe" strokeOpacity="0.28" strokeWidth="2.6" />
      <g className={dir > 0 ? "mt-gear-cw" : "mt-gear-ccw"} style={{ animationDuration: `${dur}s` }}>
        {Array.from({ length: 8 }).map((_, i) => {
          const a = (Math.PI / 4) * i;
          return (
            <line
              key={i}
              x1={r * 0.3 * Math.cos(a)}
              y1={r * 0.3 * Math.sin(a)}
              x2={r * 0.92 * Math.cos(a)}
              y2={r * 0.92 * Math.sin(a)}
              stroke="#bfdbfe"
              strokeOpacity="0.24"
              strokeWidth="2.2"
            />
          );
        })}
      </g>
    </g>
  );
}

export default function IndustrialBackground() {
  return (
    <div className="industrial-bg" aria-hidden="true">
      <svg viewBox="0 0 1600 900" preserveAspectRatio="xMidYMid slice">
        <defs>
          <pattern id="mtGrid" width="52" height="52" patternUnits="userSpaceOnUse">
            <path d="M52 0 H0 V52" fill="none" stroke="#93c5fd" strokeOpacity="0.1" strokeWidth="1" />
          </pattern>

          <linearGradient id="mtSheen" x1="0" y1="0" x2="1" y2="0">
            <stop offset="0%" stopColor="#e0f2fe" stopOpacity="0" />
            <stop offset="45%" stopColor="#e0f2fe" stopOpacity="0.7" />
            <stop offset="55%" stopColor="#ffffff" stopOpacity="0.95" />
            <stop offset="100%" stopColor="#e0f2fe" stopOpacity="0" />
          </linearGradient>

          <linearGradient id="mtMolten" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#fef08a" stopOpacity="1" />
            <stop offset="45%" stopColor="#fb923c" stopOpacity="0.95" />
            <stop offset="100%" stopColor="#ef4444" stopOpacity="0.7" />
          </linearGradient>

          <radialGradient id="mtGlow">
            <stop offset="0%" stopColor="#fbbf24" stopOpacity="0.65" />
            <stop offset="50%" stopColor="#f97316" stopOpacity="0.24" />
            <stop offset="100%" stopColor="#f97316" stopOpacity="0" />
          </radialGradient>

          <clipPath id="mtSheetClip">
            <rect x="0" y="466" width="410" height="28" />
          </clipPath>
        </defs>

        <rect width="1600" height="900" fill="url(#mtGrid)" />

        {/* ================= ARISTERI PLEURA ================= */}

        {/* Grannazia panw aristera */}
        <Gear cx={150} cy={140} rOut={104} rIn={37} teeth={15} toothW={21} toothH={21} sw={3.6} opacity={0.32} dur={44} dir={1} />
        <Gear cx={300} cy={244} rOut={64} rIn={23} teeth={12} toothW={16} toothH={16} sw={3} opacity={0.24} dur={28} dir={-1} />

        {/* ELASTRO ALOYMINIOY (aristera, meso ypsos) */}
        <g>
          <g fill="none" stroke="#bfdbfe" strokeOpacity="0.22" strokeWidth="3">
            <path d="M182 388 V572 M288 388 V572" />
            <path d="M154 572 H316" />
            <path d="M168 388 H302" />
          </g>

          {/* I lamarina */}
          <rect x="0" y="466" width="410" height="28" rx="3" fill="#93c5fd" fillOpacity="0.16" />
          <rect x="0" y="466" width="410" height="28" rx="3" fill="none" stroke="#bfdbfe" strokeOpacity="0.32" strokeWidth="2" />

          {/* I lamsi pou "trexei" panw sto metallo */}
          <g clipPath="url(#mtSheetClip)">
            <rect className="mt-sheen" x="-200" y="466" width="200" height="28" fill="url(#mtSheen)" />
          </g>

          {/* Oi dio kylindroi elasis */}
          <Roller cx={235} cy={432} r={50} dur={7} dir={1} />
          <Roller cx={235} cy={528} r={50} dur={7} dir={-1} />

          {/* Kylindroi metaforas */}
          {[330, 384, 438].map((x) => (
            <circle key={x} cx={x} cy={518} r={16} fill="none" stroke="#bfdbfe" strokeOpacity="0.22" strokeWidth="2.4" />
          ))}
        </g>

        {/* ERGOSTASIO (kato aristera) */}
        <g fill="none" stroke="#bfdbfe" strokeOpacity="0.26" strokeWidth="3.2" strokeLinejoin="round">
          <path d="M20 900 V680 H150 V740 L236 680 V900" />
          <path d="M46 680 V602 H70 V680" />
          <path d="M98 680 V626 H122 V680" />
          <path d="M46 780 h26 v26 h-26 z M96 780 h26 v26 h-26 z M46 840 h26 v26 h-26 z M96 840 h26 v26 h-26 z" />
          <path d="M172 780 h30 v30 h-30 z M172 840 h30 v30 h-30 z" />
        </g>

        {[
          { x: 58, delay: 0 },
          { x: 58, delay: 3.4 },
          { x: 110, delay: 1.7 },
          { x: 110, delay: 5.1 },
        ].map((s, i) => (
          <circle key={i} className="mt-smoke" cx={s.x} cy={596} r={13} fill="#cbd5e1" style={{ animationDelay: `${s.delay}s` }} />
        ))}

        {/* XYTIRIO (kato aristera-kentro) */}
        <g>
          <circle className="mt-glow" cx={400} cy={796} r={140} fill="url(#mtGlow)" />

          {/* Koupa (ladle) pou adeiazei */}
          <g
            fill="none"
            stroke="#fdba74"
            strokeOpacity="0.55"
            strokeWidth="3.2"
            strokeLinejoin="round"
            transform="translate(366,632) rotate(-24)"
          >
            <path d="M-46 -30 L46 -30 L30 34 L-30 34 Z" />
            <path d="M-56 -30 H56" strokeWidth="4" />
            <path d="M-62 -14 a12 12 0 0 0 -12 12 M62 -14 a12 12 0 0 1 12 12" />
          </g>

          {/* I roi tou liomenou metallou */}
          <path
            className="mt-pour"
            d="M400 614 C 398 672, 403 716, 400 754"
            stroke="url(#mtMolten)"
            strokeWidth="10"
            strokeLinecap="round"
            fill="none"
          />

          <ellipse className="mt-pool" cx={400} cy={786} rx={80} ry={17} fill="url(#mtMolten)" fillOpacity="0.7" />
          <g fill="none" stroke="#fdba74" strokeOpacity="0.42" strokeWidth="3">
            <path d="M312 786 L328 858 H472 L488 786" />
          </g>

          {[
            { x: 356, d: 0, s: 2.8 },
            { x: 384, d: 0.9, s: 2.2 },
            { x: 412, d: 1.7, s: 3.1 },
            { x: 442, d: 2.4, s: 2.4 },
            { x: 398, d: 3.1, s: 2.6 },
            { x: 370, d: 3.7, s: 2 },
          ].map((sp, i) => (
            <circle
              key={i}
              className="mt-spark"
              cx={sp.x}
              cy={776}
              r={sp.s}
              fill="#fde68a"
              style={{ animationDelay: `${sp.d}s` }}
            />
          ))}
        </g>

        {/* ================= DEXIA PLEURA ================= */}

        {/* Metritis piesis panw dexia */}
        <g fill="none" stroke="#dbeafe" strokeOpacity="0.28" strokeWidth="2.8" strokeLinecap="round">
          <circle cx={1425} cy={175} r={66} strokeWidth="3.4" />
          <circle cx={1425} cy={175} r={55} />
          <path d="M1381 205 A57 57 0 1 1 1469 205" strokeWidth="2.4" />
          <path d="M1385 139 v-10 M1425 120 v-11 M1465 139 v-10" strokeWidth="2.4" />
          <g transform="translate(1425,175)">
            <path className="mt-needle" d="M0 0 L34 -36" strokeWidth="4.2" />
          </g>
          <circle cx={1425} cy={175} r={6} fill="#dbeafe" fillOpacity="0.28" stroke="none" />
        </g>

        {/* Grannazia kato dexia */}
        <Gear cx={1470} cy={700} rOut={122} rIn={44} teeth={17} toothW={22} toothH={24} sw={3.6} opacity={0.3} dur={56} dir={-1} />
        <Gear cx={1308} cy={800} rOut={70} rIn={25} teeth={12} toothW={17} toothH={18} sw={3.2} opacity={0.22} dur={32} dir={1} />

        {/* Grannazi meso dexia */}
        <Gear cx={1330} cy={400} rOut={58} rIn={21} teeth={11} toothW={15} toothH={15} sw={3} opacity={0.18} dur={36} dir={1} />

        {/* ================= AKRES: SOLINES ================= */}

        <g fill="none" stroke="#a5f3fc" strokeOpacity="0.26" strokeWidth="5" strokeLinecap="round">
          {/* Pano akri */}
          <path d="M0 62 H520 a26 26 0 0 1 26 26 V112" />
          <path d="M1600 74 H1210 a24 24 0 0 0 -24 24 V150" />
          {/* Kato akri */}
          <path d="M0 700 a26 26 0 0 1 26 -26 H120" opacity="0" />
          <path d="M540 900 V862 a24 24 0 0 1 24 -24 H1600" />
          {/* Dexia akri */}
          <path d="M1600 330 H1400 a24 24 0 0 0 -24 24 V470" />
        </g>

        <g fill="none" stroke="#67e8f9" strokeOpacity="0.5" strokeWidth="2.6" strokeLinecap="round">
          <path className="mt-flow" d="M0 62 H520 a26 26 0 0 1 26 26 V112" strokeDasharray="14 26" />
          <path className="mt-flow mt-flow-2" d="M540 900 V862 a24 24 0 0 1 24 -24 H1600" strokeDasharray="14 26" />
          <path className="mt-flow mt-flow-3" d="M1600 74 H1210 a24 24 0 0 0 -24 24 V150" strokeDasharray="14 26" />
        </g>

        {/* Valvides */}
        <g fill="none" stroke="#a5f3fc" strokeOpacity="0.3" strokeWidth="3">
          <circle cx={546} cy={112} r={14} strokeWidth="4" />
          <path d="M532 112 H560 M546 98 V126" />
          <circle cx={1186} cy={150} r={14} strokeWidth="4" />
          <path d="M1172 150 H1200 M1186 136 V164" />
          <circle cx={1376} cy={470} r={13} strokeWidth="4" />
          <path d="M1363 470 H1389" />
        </g>
      </svg>
    </div>
  );
}
