import type { AnalysisResult } from '../schemas/analysis.schema'
import { ScoreBar } from '../components/ScoreBar'
import { Section } from '../components/Section'
import { BulletList } from '../components/BulletList'
import { TagList } from '../components/TagList'
import { CopyableText } from '../components/CopyableText'

interface ResultsPageProps {
  result: AnalysisResult
  onReset: () => void
}

export function ResultsPage({ result, onReset }: ResultsPageProps) {
  return (
    <div className="space-y-4">
      <div className="space-y-2">
        <ScoreBar label="Compatibilidad global" score={result.globalMatch} />
        <ScoreBar label="CV" score={result.cvScore} />
        <ScoreBar label="Carta de presentación" score={result.coverLetterScore} />
        <ScoreBar label="Compatibilidad ATS" score={result.atsCompatibility.score} />
      </div>

      <Section title="Fortalezas">
        <BulletList items={result.strengths} />
      </Section>

      <Section title="Debilidades">
        <BulletList items={result.weaknesses} />
      </Section>

      <Section title="Palabras clave faltantes">
        <TagList items={result.missingKeywords} />
      </Section>

      <Section title="Habilidades faltantes">
        <TagList items={result.missingSkills} />
      </Section>

      {result.atsCompatibility.issues.length > 0 && (
        <Section title="Problemas ATS">
          <BulletList items={result.atsCompatibility.issues} />
        </Section>
      )}

      <Section title="Recomendaciones">
        <BulletList items={result.recommendations} />
      </Section>

      <Section title="CV mejorado">
        <CopyableText text={result.improvedCv} />
      </Section>

      <Section title="Carta mejorada">
        <CopyableText text={result.improvedCoverLetter} />
      </Section>

      <button
        type="button"
        onClick={onReset}
        className="w-full rounded-md border border-slate-700 px-3 py-2 text-sm text-slate-200 transition hover:bg-slate-800"
      >
        Nuevo análisis
      </button>
    </div>
  )
}
