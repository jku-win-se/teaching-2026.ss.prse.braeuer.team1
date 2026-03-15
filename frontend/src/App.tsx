import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

function App() {
  const [greeting, setGreeting] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchGreeting = async () => {
    setLoading(true)
    setError(null)
    try {
      const response = await fetch('/api/hello')
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      const text = await response.text()
      setGreeting(text)
    } catch (e) {
      setError(
        'Backend nicht erreichbar. Starte das Quarkus-Backend auf Port 8080.'
      )
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchGreeting()
  }, [])

  return (
    <div className="min-h-screen bg-background flex items-center justify-center p-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle className="text-2xl">PRSE Team 1</CardTitle>
          <CardDescription>
            React + Vite + Tailwind CSS + shadcn/ui
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="rounded-lg border p-4 text-sm">
            {loading && (
              <p className="text-muted-foreground">Lade...</p>
            )}
            {error && <p className="text-destructive">{error}</p>}
            {greeting && (
              <p className="text-foreground font-medium">{greeting}</p>
            )}
          </div>
          <Button onClick={fetchGreeting} disabled={loading} className="w-full">
            Backend erneut abfragen
          </Button>
        </CardContent>
      </Card>
    </div>
  )
}

export default App
