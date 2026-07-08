interface TagListProps {
  items: string[]
}

export function TagList({ items }: TagListProps) {
  if (items.length === 0) return <p className="text-xs text-slate-500">—</p>

  return (
    <ul className="flex flex-wrap gap-1.5">
      {items.map((item) => (
        <li
          key={item}
          className="rounded-full bg-slate-800 px-2 py-0.5 text-[11px] text-slate-200"
        >
          {item}
        </li>
      ))}
    </ul>
  )
}
