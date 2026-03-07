<div align=center>

# File Storage Server
</div>

<div align=center>
<img src="./file_server.png" style="align: center; width: 300px">
</div>

<details>
<summary>German</summary>
Dieses Programm ist ein multi-threaded File Storage Server mit einer TCP-Socket-basierten Client-Server-Architektur. Der Server ermöglicht das Hochladen, Herunterladen und Löschen von Dateien. Die Architektur nutzt eine TWO-TIER Thread-Pool-Struktur (Client-Handler + Task-Worker) und implementiert SOLID-Prinzipien für wartbaren, professionellen Code.

<div align=center>

## Zu der App
</div>

**PUT:** Dateien auf dem Server hochladen und eindeutige IDs erhalten
**GET BY_ID/BY_NAME:** Dateien nach ID oder Dateiname herunterladen
**DELETE BY_ID/BY_NAME:** Dateien löschen mit automatischem Registry-Cleanup
**HTTP-ähnliche Response Codes:** 
  - *200* (OK), 
  - *400* (Bad Request), 
  - *403* (Forbidden), 
  - *404* (Not Found), 
  - *500* (Internal Error)

**Persistente File Registry:** Datei-IDs werden auf der Festplatte gespeichert und beim Neustart wiederhergestellt
**Thread-Safe:** Volatile Variablen und Synchronized Methods zur Race Condition Prevention
**Path Traversal Protection:** Sicherheitsvalidation aller Dateipfade
**SOLID-Architektur:** Strategy Pattern, Default Methods, Centralized Exception Handling

<div align=center>

## Wie kann ich diese testen?
</div>

1. **Repository klonen:**
`git clone git@github.com:Dima0687/file-storage-server.git`
2. In einer IDE **öffnen** (z.B. IntelliJ IDEA).
3. **Ausführung:**

    - **Server starten:** Führe `de.eisner.server.StorageServer` aus
    - **Client starten (separates Terminal):** Führe `de.eisner.client.StorageClient` aus

**Bedienung:**

- Wähle 2, um eine Datei hochzuladen (PUT)
- Wähle 1, um eine Datei herunterzuladen (GET BY_ID oder BY_NAME)
- Wähle 3, um eine Datei zu löschen (DELETE)
- Wähle exit, um Server und Client zu beenden



<div align=center>

## Fazit
</div>

Die größte Herausforderung war die Implementierung der TWO-TIER Thread Pool Architektur und der Thread-Safety mit Volatile Variablen. Während die theoretischen Konzepte schnell zu verstehen waren, erwies sich die fehlerfreie Umsetzung als knifflig.
Die Refaktorierung des ursprünglichen Hyperskill-Codes nach SOLID-Prinzipien (Strategy Pattern, Default Methods, Exception-basiertes Error Handling) war wertvoll. Besonders das Durcharbeiten von Race Condition Prevention und Volatile Variables zeigte, dass guter Code durch Reflektion und Überarbeitung wächst.

*Zusätzliche Eigenleistungen über die Hyperskill-Anforderungen hinaus:*

 - SOLID-Refaktorierung der ursprünglichen Lösung
 - Volatile Thread-Safety zur Race Condition Prevention
 - 3500+ Zeilen professionelle JavaDoc mit Markdown-Formatting

</br>
</br>
</details>


<details open>
<summary>English</summary>
This is a multi-threaded File Storage Server with a TCP Socket-based client-server architecture. The server enables uploading, downloading, and deleting files. The architecture uses a TWO-TIER Thread Pool structure (Client-Handler + Task-Worker) and implements SOLID principles for maintainable, professional-grade code.

<div align=center>

## About the App
</div>

**PUT:** Upload files to the server and receive unique IDs
**GET BY_ID/BY_NAME:** Download files by ID or filename
**DELETE BY_ID/BY_NAME:** Delete files with automatic registry cleanup
**HTTP-like Response Codes:**
  - *200* (OK), 
  - *400* (Bad Request),
  - *403* (Forbidden), 
  - *404* (Not Found), 
  - *500* (Internal Error)

**Persistent File Registry:** File IDs are persisted to disk and restored on server restart
**Thread-Safe:** Volatile variables and synchronized methods for race condition prevention
**Path Traversal Protection:** Security validation of all file paths
**SOLID Architecture:** Strategy Pattern, Default Methods, Centralized Exception Handling

<div align=center>

## How can I test this?
</div>

1. **Clone the repository:**
`git clone git@github.com:Dima0687/file-storage-server.git`
2. **Open** in an IDE (e.g., IntelliJ IDEA).
3. **Execution:**

    - **Start the server:** Run `de.eisner.server.StorageServer`
    - **Start the client (separate terminal):** Run `de.eisner.client.StorageClient`


**Usage:**

  - Press 2 to upload a file (PUT)
  - Press 1 to download a file (GET BY_ID or BY_NAME)
  - Press 3 to delete a file (DELETE)
  - Press exit to shutdown server and client



<div align=center>

## Conclusion
</div>
The main challenge was implementing the TWO-TIER thread pool architecture and thread safety with volatile variables. While the theoretical concepts were easy to understand, implementing them without errors proved to be tricky.

Refactoring the original Hyperskill code according to SOLID principles (strategy pattern, default methods, exception-based error handling) was valuable. Working through race condition prevention and volatile variables in particular showed that good code grows through reflection and revision.

*Additional contributions beyond the Hyperskill requirements:*

 - SOLID refactoring of the original solution
 - Volatile thread safety for race condition prevention
 - 3500+ lines of professional JavaDoc with Markdown formatting

</br>
</br>
</details>