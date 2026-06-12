param(
    [string]$Source = "Otchet_3_kurs_IT_ServiceDesk_podrobnyy_dnevnik.docx",
    [string]$Output = "Otchet_3_kurs_IT_ServiceDesk_podrobnyy_dnevnik_diagrammy.docx",
    [string]$DiagramDir = "diagrams"
)

$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

$rootDir = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$sourcePath = Join-Path $rootDir $Source
$outputPath = Join-Path $rootDir $Output
$diagramPath = Join-Path $rootDir $DiagramDir

if (-not (Test-Path -LiteralPath $sourcePath)) {
    throw "Source DOCX not found: $sourcePath"
}

$diagrams = @(
    @{
        File = "01_er_diagram.png"
        Target = "media/service_desk_er_diagram.png"
        Title = "Рисунок 1 — ER-диаграмма проекта"
        Caption = "ER-диаграмма показывает основные сущности ServiceDesk: пользователей, заявки, оборудование, подразделения и комментарии."
        Name = "ER diagram"
    },
    @{
        File = "02_application_architecture.png"
        Target = "media/service_desk_application_architecture.png"
        Title = "Рисунок 2 — Схема приложения"
        Caption = "Схема приложения показывает взаимодействие web-админки, desktop-клиента, Android-приложения, Java backend, JWT-защиты и PostgreSQL."
        Name = "Application architecture"
    },
    @{
        File = "03_database_schema.png"
        Target = "media/service_desk_database_schema.png"
        Title = "Рисунок 3 — Схема базы данных"
        Caption = "Схема базы данных отражает таблицы PostgreSQL, ключевые поля и связи по внешним ключам."
        Name = "Database schema"
    },
    @{
        File = "04_internal_interaction.png"
        Target = "media/service_desk_internal_interaction.png"
        Title = "Рисунок 4 — Схема взаимодействия внутри приложения"
        Caption = "Схема взаимодействия описывает типовой путь запроса: клиентский интерфейс, проверка JWT, controller, service, repository и база данных."
        Name = "Internal interaction"
    }
)

foreach ($diagram in $diagrams) {
    $path = Join-Path $diagramPath $diagram.File
    if (-not (Test-Path -LiteralPath $path)) {
        throw "Diagram image not found: $path"
    }
}

Copy-Item -LiteralPath $sourcePath -Destination $outputPath -Force

$wNs = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
$rNs = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"
$relNs = "http://schemas.openxmlformats.org/package/2006/relationships"
$contentTypesNs = "http://schemas.openxmlformats.org/package/2006/content-types"

function Escape-Xml([string]$value) {
    return [System.Security.SecurityElement]::Escape($value)
}

function Fragment-Node {
    param(
        [xml]$Doc,
        [string]$Xml
    )
    $fragment = $Doc.CreateDocumentFragment()
    $fragment.InnerXml = $Xml
    return $fragment.FirstChild
}

function Paragraph-Xml {
    param(
        [string]$Text,
        [int]$Size = 24,
        [bool]$Bold = $false,
        [bool]$Italic = $false,
        [string]$Align = "both",
        [int]$Before = 0,
        [int]$After = 120
    )

    $boldXml = ""
    if ($Bold) { $boldXml = "<w:b/>" }
    $italicXml = ""
    if ($Italic) { $italicXml = "<w:i/>" }

    return @"
<w:p xmlns:w="$wNs">
  <w:pPr>
    <w:spacing w:before="$Before" w:after="$After" w:line="276" w:lineRule="auto"/>
    <w:jc w:val="$Align"/>
    <w:rPr>
      <w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman" w:cs="Times New Roman"/>
      <w:sz w:val="$Size"/>
      <w:szCs w:val="$Size"/>
      $boldXml
      $italicXml
    </w:rPr>
  </w:pPr>
  <w:r>
    <w:rPr>
      <w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman" w:cs="Times New Roman"/>
      <w:sz w:val="$Size"/>
      <w:szCs w:val="$Size"/>
      $boldXml
      $italicXml
    </w:rPr>
    <w:t xml:space="preserve">$(Escape-Xml $Text)</w:t>
  </w:r>
</w:p>
"@
}

function PageBreak-Xml {
    return "<w:p xmlns:w=""$wNs""><w:r><w:br w:type=""page""/></w:r></w:p>"
}

function Image-Xml {
    param(
        [string]$RelationshipId,
        [string]$Name,
        [int]$DocPrId
    )

    $cx = 5850000
    $cy = 4021875
    return @"
<w:p xmlns:w="$wNs" xmlns:r="$rNs">
  <w:pPr>
    <w:jc w:val="center"/>
    <w:spacing w:before="80" w:after="80"/>
  </w:pPr>
  <w:r>
    <w:drawing>
      <wp:inline xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" distT="0" distB="0" distL="0" distR="0">
        <wp:extent cx="$cx" cy="$cy"/>
        <wp:effectExtent l="0" t="0" r="0" b="0"/>
        <wp:docPr id="$DocPrId" name="$(Escape-Xml $Name)"/>
        <wp:cNvGraphicFramePr>
          <a:graphicFrameLocks xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" noChangeAspect="1"/>
        </wp:cNvGraphicFramePr>
        <a:graphic xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
          <a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture">
            <pic:pic xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture">
              <pic:nvPicPr>
                <pic:cNvPr id="0" name="$(Escape-Xml $Name)"/>
                <pic:cNvPicPr/>
              </pic:nvPicPr>
              <pic:blipFill>
                <a:blip r:embed="$RelationshipId"/>
                <a:stretch>
                  <a:fillRect/>
                </a:stretch>
              </pic:blipFill>
              <pic:spPr>
                <a:xfrm>
                  <a:off x="0" y="0"/>
                  <a:ext cx="$cx" cy="$cy"/>
                </a:xfrm>
                <a:prstGeom prst="rect">
                  <a:avLst/>
                </a:prstGeom>
              </pic:spPr>
            </pic:pic>
          </a:graphicData>
        </a:graphic>
      </wp:inline>
    </w:drawing>
  </w:r>
</w:p>
"@
}

$zip = [System.IO.Compression.ZipFile]::Open($outputPath, [System.IO.Compression.ZipArchiveMode]::Update)
try {
    $contentEntry = $zip.GetEntry("[Content_Types].xml")
    $reader = [System.IO.StreamReader]::new($contentEntry.Open())
    [xml]$contentTypes = $reader.ReadToEnd()
    $reader.Close()

    $ctNs = [System.Xml.XmlNamespaceManager]::new($contentTypes.NameTable)
    $ctNs.AddNamespace("ct", $contentTypesNs)
    $pngDefault = $contentTypes.SelectSingleNode("/ct:Types/ct:Default[@Extension='png']", $ctNs)
    if ($null -eq $pngDefault) {
        $default = $contentTypes.CreateElement("Default", $contentTypesNs)
        $default.SetAttribute("Extension", "png")
        $default.SetAttribute("ContentType", "image/png")
        [void]$contentTypes.DocumentElement.AppendChild($default)
        $contentEntry.Delete()
        $contentEntry = $zip.CreateEntry("[Content_Types].xml")
        $writer = [System.IO.StreamWriter]::new($contentEntry.Open(), [System.Text.UTF8Encoding]::new($false))
        $contentTypes.Save($writer)
        $writer.Close()
    }

    $relsEntry = $zip.GetEntry("word/_rels/document.xml.rels")
    $reader = [System.IO.StreamReader]::new($relsEntry.Open())
    [xml]$rels = $reader.ReadToEnd()
    $reader.Close()

    $maxRid = 0
    foreach ($relationship in $rels.DocumentElement.ChildNodes) {
        if ($relationship.Id -match "^rId(\d+)$") {
            $maxRid = [Math]::Max($maxRid, [int]$Matches[1])
        }
    }

    for ($i = 0; $i -lt $diagrams.Count; $i++) {
        $diagram = $diagrams[$i]
        $rid = "rId$($maxRid + $i + 1)"
        $diagram.RId = $rid

        $relationship = $rels.CreateElement("Relationship", $relNs)
        $relationship.SetAttribute("Id", $rid)
        $relationship.SetAttribute("Type", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image")
        $relationship.SetAttribute("Target", $diagram.Target)
        [void]$rels.DocumentElement.AppendChild($relationship)

        $mediaEntryName = "word/$($diagram.Target)"
        $existingMedia = $zip.GetEntry($mediaEntryName)
        if ($null -ne $existingMedia) {
            $existingMedia.Delete()
        }
        $mediaEntry = $zip.CreateEntry($mediaEntryName)
        $inputStream = [System.IO.File]::OpenRead((Join-Path $diagramPath $diagram.File))
        $outputStream = $mediaEntry.Open()
        $inputStream.CopyTo($outputStream)
        $outputStream.Close()
        $inputStream.Close()
    }

    $relsEntry.Delete()
    $relsEntry = $zip.CreateEntry("word/_rels/document.xml.rels")
    $writer = [System.IO.StreamWriter]::new($relsEntry.Open(), [System.Text.UTF8Encoding]::new($false))
    $rels.Save($writer)
    $writer.Close()

    $docEntry = $zip.GetEntry("word/document.xml")
    $reader = [System.IO.StreamReader]::new($docEntry.Open())
    [xml]$doc = $reader.ReadToEnd()
    $reader.Close()

    $ns = [System.Xml.XmlNamespaceManager]::new($doc.NameTable)
    $ns.AddNamespace("w", $wNs)
    $body = $doc.SelectSingleNode("//w:body", $ns)
    $dailyTable = $doc.SelectNodes("//w:tbl", $ns).Item(1)

    $after = $dailyTable
    $nodes = @()
    $nodes += Fragment-Node -Doc $doc -Xml (PageBreak-Xml)
    $nodes += Fragment-Node -Doc $doc -Xml (Paragraph-Xml -Text "ГРАФИЧЕСКИЕ МАТЕРИАЛЫ К ПРОЕКТУ" -Size 28 -Bold $true -Align "center" -Before 0 -After 240)

    for ($i = 0; $i -lt $diagrams.Count; $i++) {
        $diagram = $diagrams[$i]
        $nodes += Fragment-Node -Doc $doc -Xml (Paragraph-Xml -Text $diagram.Title -Size 24 -Bold $true -Align "center" -Before 160 -After 80)
        $nodes += Fragment-Node -Doc $doc -Xml (Image-Xml -RelationshipId $diagram.RId -Name $diagram.Name -DocPrId (1001 + $i))
        $nodes += Fragment-Node -Doc $doc -Xml (Paragraph-Xml -Text $diagram.Caption -Size 20 -Italic $true -Align "center" -Before 0 -After 140)
        if ($i -lt $diagrams.Count - 1) {
            $nodes += Fragment-Node -Doc $doc -Xml (PageBreak-Xml)
        }
    }

    foreach ($node in $nodes) {
        $after = $body.InsertAfter($node, $after)
    }

    $docEntry.Delete()
    $docEntry = $zip.CreateEntry("word/document.xml")
    $writer = [System.IO.StreamWriter]::new($docEntry.Open(), [System.Text.UTF8Encoding]::new($false))
    $doc.Save($writer)
    $writer.Close()
}
finally {
    $zip.Dispose()
}

Write-Output $outputPath

