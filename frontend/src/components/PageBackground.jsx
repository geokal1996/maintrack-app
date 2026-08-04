// Diakritiko "ydatografima" stis DYO PLEURES tis selidas - allo motivo se kathe
// selida.
//
// ARXI SXEDIASMOU: to platos kathe loridas ypologizetai apo to CSS san
// "oso xoros perisevei dexia kai aristera apo to periexomeno". Etsi to sxedio
// DEN mpainei POTE kato apo kartes, pinakes i diagrammata - kai an i othoni
// einai mikri kai den perissevei xoros, to platos ginetai 0 kai apla eksafanizetai.

const S = {
  fill: "none",
  stroke: "var(--art)",
  strokeLinecap: "round",
  strokeLinejoin: "round",
};

/* ---------------- Vasika sximata ---------------- */

function Gear({ cx, cy, r, teeth = 12, tw = 16, th = 16, sw = 3 }) {
  return (
    <g transform={`translate(${cx},${cy})`} {...S} strokeWidth={sw}>
      <circle r={r} />
      <circle r={r * 0.34} />
      {Array.from({ length: teeth }).map((_, i) => (
        <rect
          key={i}
          x={-tw / 2}
          y={-(r + th)}
          width={tw}
          height={th + sw}
          rx="2"
          transform={`rotate(${(360 / teeth) * i})`}
        />
      ))}
      {Array.from({ length: 6 }).map((_, i) => {
        const a = (Math.PI / 3) * i;
        return (
          <line
            key={i}
            x1={r * 0.34 * Math.cos(a)}
            y1={r * 0.34 * Math.sin(a)}
            x2={(r - 4) * Math.cos(a)}
            y2={(r - 4) * Math.sin(a)}
          />
        );
      })}
    </g>
  );
}

function Gauge({ cx, cy, r, angle = -45 }) {
  const a = (angle * Math.PI) / 180;
  return (
    <g transform={`translate(${cx},${cy})`} {...S} strokeWidth="2.8">
      <circle r={r} strokeWidth="3.2" />
      <circle r={r * 0.84} />
      <path d={`M${-r * 0.68} ${r * 0.45} A${r * 0.82} ${r * 0.82} 0 1 1 ${r * 0.68} ${r * 0.45}`} strokeWidth="2.2" />
      <line x1="0" y1="0" x2={r * 0.6 * Math.cos(a)} y2={r * 0.6 * Math.sin(a)} strokeWidth="3.6" />
      <circle r="3.5" />
      {[-140, -110, -70, -40].map((d) => {
        const t = (d * Math.PI) / 180;
        return (
          <line
            key={d}
            x1={r * 0.86 * Math.cos(t)}
            y1={r * 0.86 * Math.sin(t)}
            x2={r * 0.98 * Math.cos(t)}
            y2={r * 0.98 * Math.sin(t)}
            strokeWidth="2.2"
          />
        );
      })}
    </g>
  );
}

function Bolt({ cx, cy, r = 14 }) {
  const pts = Array.from({ length: 6 })
    .map((_, i) => {
      const a = (Math.PI / 3) * i - Math.PI / 6;
      return `${(cx + r * Math.cos(a)).toFixed(1)},${(cy + r * Math.sin(a)).toFixed(1)}`;
    })
    .join(" ");
  return (
    <g {...S} strokeWidth="2.6">
      <polygon points={pts} />
      <circle cx={cx} cy={cy} r={r * 0.42} />
    </g>
  );
}

function Valve({ cx, cy, r = 16 }) {
  return (
    <g {...S} strokeWidth="3">
      <circle cx={cx} cy={cy} r={r} />
      <path d={`M${cx - r} ${cy} H${cx + r} M${cx} ${cy - r} V${cy + r}`} strokeWidth="2.4" />
    </g>
  );
}

/* ---------------- DASHBOARD ---------------- */

function DashboardLeft() {
  return (
    <>
      <Gauge cx={150} cy={140} r={82} angle={-52} />
      <g {...S} strokeWidth="2.6">
        <path d="M150 232 V320" />
      </g>
      <Gauge cx={150} cy={396} r={54} angle={-12} />

      {/* Mini rabdogramma */}
      <g {...S} strokeWidth="2.8">
        <path d="M52 700 V560" strokeDasharray="7 9" />
        <path d="M52 700 H252" />
        {[
          { x: 66, h: 96 },
          { x: 112, h: 128 },
          { x: 158, h: 72 },
          { x: 204, h: 110 },
        ].map((b) => (
          <rect key={b.x} x={b.x} y={700 - b.h} width="32" height={b.h} rx="3" />
        ))}
      </g>

      {/* Grammi tasis */}
      <g {...S} strokeWidth="3">
        <path d="M46 900 L104 848 L150 872 L206 796 L256 824" />
        {[104, 150, 206].map((x, i) => (
          <circle key={x} cx={x} cy={[848, 872, 796][i]} r="5" />
        ))}
      </g>

      <Bolt cx={70} cy={1010} r={16} />
      <Bolt cx={150} cy={1074} r={12} />
      <Bolt cx={232} cy={1010} r={16} />
      <g {...S} strokeWidth="2.4">
        <path d="M70 1010 L150 1074 L232 1010" strokeDasharray="6 10" />
      </g>
    </>
  );
}

function DashboardRight() {
  return (
    <>
      <g {...S} strokeWidth="2.6">
        <path d="M150 0 V200" strokeDasharray="10 12" />
      </g>
      <Valve cx={150} cy={230} r={20} />

      {/* Katheti grammi tasis */}
      <g {...S} strokeWidth="3">
        <path d="M70 300 L200 372 L96 448 L214 522 L84 596 L196 668" />
        {[
          [200, 372],
          [96, 448],
          [214, 522],
          [84, 596],
        ].map(([x, y]) => (
          <circle key={`${x}-${y}`} cx={x} cy={y} r="5" />
        ))}
      </g>

      <Gauge cx={150} cy={790} r={68} angle={-100} />

      <g {...S} strokeWidth="2.8">
        <path d="M56 900 H244 M56 950 H244 M56 1000 H180" strokeDasharray="14 10" />
      </g>

      <Gear cx={150} cy={1120} r={62} teeth={12} tw={16} th={16} sw={3} />
    </>
  );
}

/* ---------------- MHXANES ---------------- */

function MachinesLeft() {
  return (
    <>
      <Gear cx={130} cy={120} r={86} teeth={13} tw={19} th={19} sw={3.4} />
      <Gear cx={238} cy={252} r={52} teeth={10} tw={14} th={14} sw={3} />
      <Gear cx={112} cy={352} r={40} teeth={9} tw={12} th={12} sw={2.8} />

      {/* Solinas pou katevainei */}
      <g {...S} strokeWidth="4">
        <path d="M150 420 V520 a24 24 0 0 0 24 24 H240" />
      </g>
      <Valve cx={150} cy={470} r={17} />

      {/* Kylindros elasis */}
      <g {...S} strokeWidth="3">
        <path d="M40 600 H260" strokeWidth="4" />
        <circle cx={150} cy={660} r={52} />
        <circle cx={150} cy={660} r={16} />
        {Array.from({ length: 8 }).map((_, i) => {
          const a = (Math.PI / 4) * i;
          return (
            <line
              key={i}
              x1={150 + 18 * Math.cos(a)}
              y1={660 + 18 * Math.sin(a)}
              x2={150 + 48 * Math.cos(a)}
              y2={660 + 48 * Math.sin(a)}
              strokeWidth="2.4"
            />
          );
        })}
        <path d="M40 726 H260" strokeWidth="4" />
      </g>

      <Bolt cx={62} cy={800} r={15} />
      <Bolt cx={150} cy={800} r={15} />
      <Bolt cx={238} cy={800} r={15} />

      {/* Presa */}
      <g {...S} strokeWidth="3.2">
        <path d="M46 1150 H254 M70 1150 V1030 M230 1150 V1030 M46 1030 H254" />
        <path d="M92 1030 V962 H208 V1030" />
        <path d="M150 962 V892" strokeWidth="5" />
        <circle cx={150} cy={868} r={24} />
      </g>
    </>
  );
}

function MachinesRight() {
  return (
    <>
      <g {...S} strokeWidth="4">
        <path d="M150 0 V90 a24 24 0 0 1 -24 24 H40" />
      </g>
      <Valve cx={150} cy={60} r={18} />

      <Gear cx={160} cy={220} r={72} teeth={12} tw={18} th={18} sw={3.2} />
      <Gear cx={64} cy={340} r={44} teeth={10} tw={13} th={13} sw={2.8} />

      {/* Iman metaforas */}
      <g {...S} strokeWidth="3">
        <circle cx={72} cy={480} r={34} />
        <circle cx={228} cy={480} r={34} />
        <path d="M72 446 H228 M72 514 H228" />
        <path d="M72 446 a34 34 0 0 0 0 68 M228 446 a34 34 0 0 1 0 68" />
      </g>

      {/* Kivotia panw ston imanta */}
      <g {...S} strokeWidth="2.6">
        <rect x={88} y={402} width="40" height="40" rx="4" />
        <rect x={168} y={402} width="40" height="40" rx="4" />
      </g>

      <Bolt cx={150} cy={600} r={18} />

      {/* Kinitiras */}
      <g {...S} strokeWidth="3.2">
        <rect x={62} y={680} width="176" height="120" rx="14" />
        <path d="M92 680 V650 H208 V680" />
        <path d="M238 712 h34 v56 h-34" />
        {[706, 730, 754, 778].map((y) => (
          <path key={y} d={`M84 ${y} H216`} strokeWidth="2.2" />
        ))}
      </g>

      <g {...S} strokeWidth="4">
        <path d="M150 800 V900" />
      </g>

      <Gear cx={150} cy={1000} r={78} teeth={13} tw={19} th={19} sw={3.4} />
      <Gear cx={252} cy={1128} r={46} teeth={10} tw={13} th={13} sw={2.8} />
    </>
  );
}

/* ---------------- VLAVES ---------------- */

function FaultsLeft() {
  return (
    <>
      <g {...S} strokeWidth="3.4">
        <path d="M150 40 L262 236 H38 Z" />
        <path d="M150 108 V166" strokeWidth="4.6" />
        <circle cx={150} cy={196} r={4.5} strokeWidth="4.6" />
      </g>

      {/* Gallikó kleidi */}
      <g {...S} strokeWidth="3.4" transform="translate(150,420) rotate(-30)">
        <path d="M-16 -120 a36 36 0 1 0 32 0 V-8 a16 16 0 0 1 -16 16 a16 16 0 0 1 -16 -16 Z" />
        <path d="M-16 -120 H16" />
      </g>

      <Bolt cx={64} cy={580} r={16} />
      <Bolt cx={236} cy={620} r={13} />

      {/* Katsavidi */}
      <g {...S} strokeWidth="3.2" transform="translate(150,780) rotate(18)">
        <path d="M-18 -110 h36 v70 h-36 z" />
        <path d="M-8 -40 h16 v96 h-16 z" />
        <path d="M-8 56 h16 v22 l-8 12 l-8 -12 z" />
      </g>

      {/* Grammi vlavis pou "spaei" */}
      <g {...S} strokeWidth="3">
        <path d="M40 940 H120 L146 906 L172 974 L198 940 H260" />
      </g>

      <g {...S} strokeWidth="3.2">
        <path d="M150 1010 L246 1178 H54 Z" />
        <path d="M150 1068 V1112" strokeWidth="4.2" />
        <circle cx={150} cy={1138} r={4} strokeWidth="4.2" />
      </g>
    </>
  );
}

function FaultsRight() {
  return (
    <>
      <Gauge cx={150} cy={130} r={72} angle={-25} />

      {/* Kleidi solinon */}
      <g {...S} strokeWidth="3.4" transform="translate(150,400) rotate(22)">
        <path d="M0 -140 a28 28 0 0 1 28 28 l-18 18 l18 18 l-18 18 l18 18 l-18 18 V96 a12 12 0 0 1 -24 0 V-22 l-18 -18 l18 -18 l-18 -18 l18 -18 l-18 -18 a28 28 0 0 1 28 -28 Z" />
      </g>

      <Bolt cx={70} cy={560} r={14} />
      <Bolt cx={232} cy={596} r={17} />

      {/* Lista elenxou */}
      <g {...S} strokeWidth="2.8">
        {[700, 760, 820, 880].map((y, i) => (
          <g key={y}>
            <rect x={52} y={y - 18} width="36" height="36" rx="7" />
            {i < 3 && <path d={`M60 ${y} l7 8 l14 -18`} strokeWidth="3.2" />}
            <path d={`M106 ${y} H250`} strokeDasharray="12 9" />
          </g>
        ))}
      </g>

      {/* Kleidi Allen */}
      <g {...S} strokeWidth="4.4">
        <path d="M92 1000 V1120 H216" />
      </g>

      <g {...S} strokeWidth="3.2">
        <path d="M150 1160 L150 1200" strokeDasharray="8 8" />
      </g>
    </>
  );
}

/* ---------------- PARETO ---------------- */

function ParetoLeft() {
  return (
    <>
      {/* Rabdogramma me kampyli */}
      <g {...S} strokeWidth="2.8">
        <path d="M44 60 V330 H262" />
        {[
          { x: 58, h: 224 },
          { x: 110, h: 168 },
          { x: 162, h: 116 },
          { x: 214, h: 74 },
        ].map((b) => (
          <rect key={b.x} x={b.x} y={330 - b.h} width="38" height={b.h} rx="3" />
        ))}
        <path d="M77 250 L129 194 L181 158 L233 134" strokeWidth="3.2" />
        {[77, 129, 181, 233].map((x, i) => (
          <circle key={x} cx={x} cy={[250, 194, 158, 134][i]} r="5" />
        ))}
      </g>

      {/* Grammi 80% */}
      <g {...S} strokeWidth="2.6">
        <path d="M44 430 H262" strokeDasharray="10 12" />
        <text x="44" y="410" fill="var(--art)" stroke="none" fontSize="38" fontWeight="700" fontFamily="inherit">
          80%
        </text>
      </g>

      {/* Katheto rabdogramma */}
      <g {...S} strokeWidth="2.8">
        <path d="M60 540 V860" />
        {[
          { y: 560, w: 190 },
          { y: 618, w: 148 },
          { y: 676, w: 112 },
          { y: 734, w: 78 },
          { y: 792, w: 46 },
        ].map((b) => (
          <rect key={b.y} x={60} y={b.y} width={b.w} height="38" rx="3" />
        ))}
      </g>

      <Gauge cx={150} cy={1000} r={66} angle={-30} />

      <g {...S} strokeWidth="2.6">
        <path d="M44 1110 H256 M44 1160 H190" strokeDasharray="12 10" />
      </g>
    </>
  );
}

function ParetoRight() {
  return (
    <>
      <g {...S} strokeWidth="2.6">
        <path d="M52 40 H248 M52 90 H196 M52 140 H248" strokeDasharray="13 10" />
      </g>

      {/* Athroistiki kampyli */}
      <g {...S} strokeWidth="3">
        <path d="M48 480 V210" strokeDasharray="8 10" />
        <path d="M48 480 H256" />
        <path d="M48 452 C 106 340, 168 262, 252 236" strokeWidth="3.4" />
        {[
          [106, 372],
          [168, 288],
          [252, 236],
        ].map(([x, y]) => (
          <circle key={x} cx={x} cy={y} r="5.5" />
        ))}
      </g>

      {/* Pita */}
      <g {...S} strokeWidth="3">
        <circle cx={150} cy={660} r={84} />
        <circle cx={150} cy={660} r={44} />
        <path d="M150 576 V616 M234 660 H194 M150 744 V704 M66 660 H106" strokeWidth="2.4" />
        <path d="M150 576 A84 84 0 0 1 234 660" strokeWidth="5" />
      </g>

      {/* Rabdoi */}
      <g {...S} strokeWidth="2.8">
        <path d="M52 1000 V840" />
        <path d="M52 1000 H256" />
        {[
          { x: 66, h: 128 },
          { x: 114, h: 96 },
          { x: 162, h: 64 },
          { x: 210, h: 38 },
        ].map((b) => (
          <rect key={b.x} x={b.x} y={1000 - b.h} width="34" height={b.h} rx="3" />
        ))}
      </g>

      <g {...S} strokeWidth="2.6">
        <path d="M52 1090 H248 M52 1140 H180" strokeDasharray="12 10" />
      </g>
    </>
  );
}

/* ---------------- XRISTES ---------------- */

function UsersLeft() {
  return (
    <>
      {/* Kranos ergasias */}
      <g {...S} strokeWidth="3.4">
        <path d="M42 216 a108 108 0 0 1 216 0 Z" />
        <path d="M16 216 H284" strokeWidth="4.6" />
        <path d="M150 108 V68" />
        <path d="M110 122 a48 48 0 0 1 80 0" strokeWidth="2.6" />
      </g>

      {/* Karta prosopikou */}
      <g {...S} strokeWidth="3.2">
        <rect x={56} y={360} width="188" height="242" rx="16" />
        <path d="M130 330 H170 a12 12 0 0 1 12 12 V360 h-64 v-18 a12 12 0 0 1 12 -12 Z" />
        <circle cx={150} cy={438} r={32} />
        <path d="M102 522 a48 48 0 0 1 96 0" />
        <path d="M96 556 H204" strokeWidth="2.4" />
      </g>

      {/* Omada */}
      <g {...S} strokeWidth="3">
        {[
          { x: 76, r: 26 },
          { x: 150, r: 32 },
          { x: 224, r: 26 },
        ].map((p) => (
          <g key={p.x}>
            <circle cx={p.x} cy={720} r={p.r} />
            <path d={`M${p.x - p.r * 1.5} ${800} a${p.r * 1.5} ${p.r * 1.5} 0 0 1 ${p.r * 3} 0`} />
          </g>
        ))}
      </g>

      {/* Ierarxia */}
      <g {...S} strokeWidth="2.8">
        <rect x={104} y={900} width="92" height="46" rx="9" />
        <path d="M150 946 V986 M70 986 H230 M70 986 V1016 M230 986 V1016" strokeDasharray="0" />
        <rect x={26} y={1016} width="88" height="44" rx="9" />
        <rect x={186} y={1016} width="88" height="44" rx="9" />
      </g>
    </>
  );
}

function UsersRight() {
  return (
    <>
      <g {...S} strokeWidth="3.2">
        <rect x={54} y={60} width="170" height="216" rx="15" />
        <circle cx={139} cy={132} r={28} />
        <path d="M97 208 a42 42 0 0 1 84 0" />
        <path d="M92 240 H186" strokeWidth="2.4" />
      </g>
      <g {...S} strokeWidth="3" opacity="0.65">
        <rect x={110} y={110} width="170" height="216" rx="15" />
      </g>

      {/* Kleidaria / dikaiomata */}
      <g {...S} strokeWidth="3.4">
        <rect x={82} y={470} width="136" height="112" rx="14" />
        <path d="M110 470 V430 a40 40 0 0 1 80 0 V470" />
        <circle cx={150} cy={518} r={13} />
        <path d="M150 531 V552" />
      </g>

      {/* Kranos */}
      <g {...S} strokeWidth="3.2">
        <path d="M62 760 a88 88 0 0 1 176 0 Z" />
        <path d="M40 760 H260" strokeWidth="4.2" />
        <path d="M150 672 V640" />
      </g>

      {/* Lista xriston */}
      <g {...S} strokeWidth="2.8">
        {[880, 950, 1020, 1090].map((y) => (
          <g key={y}>
            <circle cx={76} cy={y} r={20} />
            <path d={`M112 ${y - 10} H250`} strokeDasharray="13 9" />
            <path d={`M112 ${y + 10} H198`} strokeDasharray="13 9" strokeWidth="2.2" />
          </g>
        ))}
      </g>
    </>
  );
}

/* ---------------- Sinthesi ---------------- */

const VARIANTS = {
  dashboard: [DashboardLeft, DashboardRight],
  machines: [MachinesLeft, MachinesRight],
  faults: [FaultsLeft, FaultsRight],
  pareto: [ParetoLeft, ParetoRight],
  users: [UsersLeft, UsersRight],
};

export default function PageBackground({ variant = "dashboard" }) {
  const [Left, Right] = VARIANTS[variant] || VARIANTS.dashboard;

  return (
    <div className="page-art" aria-hidden="true">
      {/* To key ksanaftiaxnei to SVG se kathe allagi selidas, oste na paizei
          apo tin arxi to diskretiko fade-in. */}
      <div className="art-side art-left" key={`${variant}-l`}>
        <svg viewBox="0 0 300 1200" preserveAspectRatio="xMidYMid slice">
          <Left />
        </svg>
      </div>
      <div className="art-side art-right" key={`${variant}-r`}>
        <svg viewBox="0 0 300 1200" preserveAspectRatio="xMidYMid slice">
          <Right />
        </svg>
      </div>
    </div>
  );
}
