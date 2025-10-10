# pkutils

PKUtils is a utility mod for the PunicaKitty roleplay server, inspired by the former UnicacityAddon. It provides useful commands, QoL features, and custom tools tailored for PunicaKitty, enhancing gameplay and making everyday interactions smoother and more enjoyable.

## Features & Funktionen

### Allgemein
- Sollte ein Command mit einem Großbuchstaben eingegeben werden (z. B. `/Afk` statt `/afk`) wird dieser Command automatisch zu einem gültigen Command umgewandelt
- Die Tabliste wird nach Teammitgliedern und Fraktionen (FBI, Polizei, Rettungsdienst, News) sortiert
- Als Business-Besitzer wird in der Business-Info ein Button angezeigt, um die Einnahmen direkt abzubuchen
- Wirft man eine Glasflasche in der Nähe eines Shops weg, wird diese als Pfand abgegeben
- Es wird angezeigt wie lang der Cooldown für Bandagen, Schmerzpillen und Absorption ist
- Es werden Sounds abgespielt für Notrufe, Bomben und weitere Situationen

### Auto
- Das Auf-/Abschließen eines Fahrzeuges wurde teilweise automatisiert (automatisches Klicken des Items im Inventar)
- Beim Rechtsklick auf das eigene Fahrzeug wird automatisch `/car lock` ausgeführt
- Steigt man in ein Fahrzeug ein, wird dieses automatisch gestartet und abgeschlossen

### Fraktionen
- Hinter dem Spielernamen befindet sich ein Symbol, um die dazugehörige Fraktion zu erkennen (auch für bewusstlose Personen)
- Sollte ein Spieler Contract, auf der Blacklist stehen, Hausverbot oder Wanted-Punkte haben, wird der Name dementsprechend eingefärbt und in der Spielerliste angezeigt
- Es werden für alle Fraktionen verschiedene Aktivitäten wie Reinforcements, Verhaftungen, Wiederbelebungen und weiteres gezählt
- Das Design der Reinforcements ist so überarbeitet, dass diese besser auffallen
- Für das FBI, die Polizei und den Rettungsdienst gibt es einen Timer, der die Dauer der Bombe anzeigt
- Für die Polizei wurde ein Minuspunkte-System hinzugefügt
- Mit einem Rechtsklick auf ein Fraktionstor (nicht Fraktionstür) wird dieses automatisch geöffnet oder geschlossen

### Jobs
- Bei Transport-Jobs werden nach der Eingabe des `/droptransport` alle weiteren Kisten automatisch abgegeben
- Beim Hochseefischer-Job wird das Netz automatisch ausgeworfen und der gefangene Fisch am Ende automatisch abgegeben
- Für den Transport des Tabaks zur Shisha-Bar wird der Tabak am Ende automatisch abgegeben
- Der Müllmann-Job gibt am Ende den gesammelten Müll automatisch ab, ohne dass der `/dropwaste` Command ausgeführt werden muss
- Für den Holzfäller-Job wird automatisch `/findtree` ausgeführt, um Bäume leichter zu finden, die sich zum Fällen eignen
- Für den Pizzalieferanten-Job wird `/getpizza` automatisch ausgeführt, bis 10 Pizzen gesammelt wurden
- Es werden Countdowns angezeigt, bis ein Job wieder ausgeführt werden kann
- Bei der Abgabe von Uran am Atomkraftwerk muss man nicht mehr aus dem Auto aussteigen

### Widgets
- Aktuelles Datum und Uhrzeit
- Informationen über den PayDay (Dauer bis zum nächsten PayDay, Gehalt und Erfahrung)
- Status des Autos (offen/abgeschlossen)
- Geld auf der Hand und auf der Bank

## Befehle

**Allgemein**

| Befehl                           | Beschreibung                                                                           |
|----------------------------------|----------------------------------------------------------------------------------------|
| `/pkutils`                       | Zeigt nützliche Status-Informationen über das Projekt an                               |
| `/sync`                          | Startet die Synchronisierung um Daten wie Fraktionsmitglieder mit der Mod abzugleichen |
| `/todo`                          | Zeigt eine Todoliste an (`/todo add <Aufgabe>`)                                        |
| `/mi`                            | Alias für `/memberinfo`                                                                |
| `/mia`                           | Alias für `/memberinfoall`                                                             |
| `/todo`  (`/todo add <Aufgabe>`) | Zeigt eine Todoliste an                                                                |

**Chat**

| Befehl | Beschreibung                                                                                                   |
|--------|----------------------------------------------------------------------------------------------------------------|
| `/ff`  | Aktiviert und deaktiviert das dauerhafte Schreiben im F-Chat ohne den `/f` Befehl jedes Mal eingeben zu müssen |
| `/dd`  | Aktiviert und deaktiviert das dauerhafte Schreiben im D-Chat ohne den `/d` Befehl jedes Mal eingeben zu müssen |
| `/ww`  | Aktiviert und deaktiviert das dauerhafte Flüstern ohne den `/w` Befehl jedes Mal eingeben zu müssen            |

**Fraktionen**

| Befehl                                                                                       | Beschreibung                                                                                                                                      |
|----------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| `/activity <optional: Wochenindex>` `/activity player <Spielername> <optional: Wochenindex>` | Zeigt die eigene oder die Aktivität eines Spielern an. Wochenindex ist dabei für die aktuelle Woche 0 und für die letzte Woche -1 usw.            |
| `/asetbl`                                                                                    | Setzt Blacklist-Gründe automatisch (Blacklist-Gründe können mit einem Ticket auf dem Discord beantragt werden)                                    |
| `/equipped`                                                                                  | Zeigt das ausgerüstete Equip eines Spielers an                                                                                                    |
| `/eigenbedarf`                                                                               | Nimmt eine eingestellte Menge an Drogen aus der Drogenbank einer Fraktion oder gibt diese an einen Spieler                                        |
| `schwarzmarkt`                                                                               | Zeigt alle Schwarzmärkte an einschließlich des Zeitpunkts des letzten Besuchs des Ortes und einer Markierung ob sich der Schwarzmarkt dort befand |

**Geld**

| Befehl           | Beschreibung                                                                                                                                                                         |
|------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `/einzahlen`     | Zahlt das gesamte Bargeld in den ATM in der Nähe ein                                                                                                                                 |
| `/reichensteuer` | Bucht so viel geld vom Konto ab bis nur noch 100000$ auf diesem sind oder der ATM leer ist                                                                                           |
| `/adropmoney`    | Bucht für den Geldtransport-Job 16000\$ vom Konto ab, sodass das Geld vom Geldtransport-Job in den ATM eingezahlt werden kann und bucht die 16000$ anschließend zurück auf das Konto |

**Handy**

| Befehl                            | Beschreibung                                                                       |
|-----------------------------------|------------------------------------------------------------------------------------|
| `/acall <Spielername>`            | Ermöglicht das Anrufen mittels Spielername statt der Nummer                        |
| `/asms <Spielername> <Nachricht>` | Ermöglicht das Schreiben einer SMS mittels Spielername statt der Nummer            |
| `/reply <Nachricht>`              | Antwortet direkt mit einer SMS an den Spieler der zuletzt eine SMS geschrieben hat |
