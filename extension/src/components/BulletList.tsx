interface BulletListProps {
  items: string[]
}

export function BulletList({ items }: BulletListProps) {
  if (items.length === 0) return <p className="text-xs text-slate-500">—</p>

  return (
    <ul className="list-disc space-y-1 pl-4 text-xs leading-relaxed text-slate-300">
      {items.map((item, index) => (
        <li key={`${index}-${item.slice(0, 24)}`}>{item}</li>
      ))}
    </ul>
  )
}
