param(
    [string]$Source = "Otchet_3_kurs_IT_servicedesk_source.docx",
    [string]$Output = "Otchet_3_kurs_IT_ServiceDesk_podrobnyy_dnevnik.docx"
)

$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

$workDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$rootDir = Split-Path -Parent $workDir
$sourcePath = Join-Path $rootDir $Source
$outputPath = Join-Path $rootDir $Output

if (-not (Test-Path -LiteralPath $sourcePath)) {
    throw "Source DOCX not found: $sourcePath"
}

Copy-Item -LiteralPath $sourcePath -Destination $outputPath -Force

$namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
$relNamespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"

function New-WElement {
    param(
        [xml]$Doc,
        [string]$Name
    )
    $element = $Doc.CreateElement("w", $Name, $namespace)
    Write-Output -NoEnumerate $element
}

function New-WTextParagraph {
    param(
        [xml]$Doc,
        [string]$Text,
        [bool]$Bold = $false
    )

    $p = New-WElement -Doc $Doc -Name "p"
    $pPr = New-WElement -Doc $Doc -Name "pPr"
    $spacing = New-WElement -Doc $Doc -Name "spacing"
    $spacing.SetAttribute("after", $namespace, "80")
    $spacing.SetAttribute("line", $namespace, "260")
    $spacing.SetAttribute("lineRule", $namespace, "auto")
    [void]$pPr.AppendChild($spacing)
    [void]$p.AppendChild($pPr)

    $r = New-WElement -Doc $Doc -Name "r"
    $rPr = New-WElement -Doc $Doc -Name "rPr"
    $rFonts = New-WElement -Doc $Doc -Name "rFonts"
    $rFonts.SetAttribute("ascii", $namespace, "Calibri")
    $rFonts.SetAttribute("hAnsi", $namespace, "Calibri")
    $sz = New-WElement -Doc $Doc -Name "sz"
    $sz.SetAttribute("val", $namespace, "20")
    [void]$rPr.AppendChild($rFonts)
    [void]$rPr.AppendChild($sz)
    if ($Bold) {
        [void]$rPr.AppendChild((New-WElement -Doc $Doc -Name "b"))
    }
    [void]$r.AppendChild($rPr)

    $t = New-WElement -Doc $Doc -Name "t"
    $spaceAttribute = $Doc.CreateAttribute("xml", "space", "http://www.w3.org/XML/1998/namespace")
    $spaceAttribute.Value = "preserve"
    [void]$t.Attributes.Append($spaceAttribute)
    $t.InnerText = $Text
    [void]$r.AppendChild($t)
    [void]$p.AppendChild($r)

    Write-Output -NoEnumerate $p
}

function Set-CellParagraphs {
    param(
        [xml]$Doc,
        [System.Xml.XmlElement]$Cell,
        [string[]]$Paragraphs
    )

    $children = @($Cell.ChildNodes)
    foreach ($child in $children) {
        if ($child.LocalName -ne "tcPr") {
            [void]$Cell.RemoveChild($child)
        }
    }

    foreach ($paragraph in $Paragraphs) {
        $isLead = $paragraph.StartsWith("Выполнено:") -or $paragraph.StartsWith("Результат:")
        $paragraphNode = @(New-WTextParagraph -Doc $Doc -Text $paragraph -Bold:$isLead)[-1]
        [void]$Cell.AppendChild([System.Xml.XmlNode]$paragraphNode)
    }
}

$entries = @(
    @{
        Date = "25.05."
        Paragraphs = @(
            "Выполнено: изучена предметная область корпоративной службы технической поддержки и учета оборудования. Определены основные пользователи системы: сотрудник, техник и администратор.",
            "Результат: сформулирована тема проекта «Corporate ServiceDesk — информационная система учета заявок и оборудования организации», определены цели, задачи, границы проекта и основные бизнес-сценарии."
        )
    },
    @{
        Date = "26.05."
        Paragraphs = @(
            "Выполнено: проведен анализ требований к будущей информационной системе. Выделены функции авторизации, регистрации заявок, изменения статусов, комментирования, учета оборудования и формирования отчетов.",
            "Результат: подготовлена структура технического задания, описаны роли пользователей, функциональные и нефункциональные требования, выбран стек Java Spring Boot, PostgreSQL, Kotlin, Android и Docker."
        )
    },
    @{
        Date = "27.05."
        Paragraphs = @(
            "Выполнено: спроектирована архитектура приложения. Backend выделен в отдельный REST API, web-интерфейс — в админ-панель, desktop-клиент — в приложение администратора, mobile-клиент — в приложение техника.",
            "Результат: определены модули проекта, схема взаимодействия клиентов с API, принципы обмена данными через JSON и порядок защиты запросов с помощью JWT-токенов."
        )
    },
    @{
        Date = "28.05."
        Paragraphs = @(
            "Выполнено: разработана логическая модель базы данных. Определены таблицы users, departments, equipment, tickets и ticket_comments, связи между пользователями, заявками, комментариями и оборудованием.",
            "Результат: подготовлена ER-структура базы PostgreSQL, перечислены ключевые поля сущностей, определены статусы заявок и оборудования, а также связи many-to-one и one-to-many."
        )
    },
    @{
        Date = "29.05."
        Paragraphs = @(
            "Выполнено: создана структура backend-проекта на Java Spring Boot с Maven. Подключены зависимости Spring Web, Spring Security, Spring Data JPA, Validation, PostgreSQL Driver, OpenAPI и JUnit.",
            "Результат: сформирован каркас серверной части, настроен application.yml, задан контекст API /api, параметры подключения к PostgreSQL и базовые настройки JWT и CORS."
        )
    },
    @{
        Date = "01.06."
        Paragraphs = @(
            "Выполнено: реализованы доменные перечисления для ролей, статусов заявок, приоритетов заявок, типов оборудования и состояний оборудования. Это позволило избежать хранения случайных строк в бизнес-логике.",
            "Результат: подготовлен единый набор значений ADMIN, TECHNICIAN, EMPLOYEE, NEW, IN_PROGRESS, RESOLVED, CLOSED, LAPTOP, PRINTER, IN_SERVICE, REPAIR и других справочных значений."
        )
    },
    @{
        Date = "02.06."
        Paragraphs = @(
            "Выполнено: реализованы JPA-сущности UserAccount, Department, Equipment, Ticket и TicketComment. Для заявок добавлены даты создания, обновления и закрытия, а для комментариев — автор и время создания.",
            "Результат: backend получил объектную модель данных, связанную с таблицами PostgreSQL через Hibernate. Настроены связи между заявками, пользователями, оборудованием и комментариями."
        )
    },
    @{
        Date = "03.06."
        Paragraphs = @(
            "Выполнено: созданы репозитории Spring Data JPA для доступа к пользователям, заявкам, комментариям, оборудованию и подразделениям. Добавлены методы поиска по статусу и автору заявки.",
            "Результат: реализован слой доступа к данным без ручного SQL, подготовлены методы для списков заявок, фильтрации, подсчета статусов и получения комментариев в хронологическом порядке."
        )
    },
    @{
        Date = "04.06."
        Paragraphs = @(
            "Выполнено: разработана JWT-авторизация. Реализованы JwtService, фильтр JwtAuthenticationFilter, UserService для загрузки пользователя и SecurityConfig для защиты REST API.",
            "Результат: пользователь может войти по логину и паролю, получить JWT-токен и обращаться к защищенным endpoint-ам. Публичным оставлен только вход в систему и Swagger-документация."
        )
    },
    @{
        Date = "05.06."
        Paragraphs = @(
            "Выполнено: реализован AuthController и DTO для авторизации. Настроена выдача ответа с токеном и данными пользователя: идентификатором, логином, ФИО, email и ролью.",
            "Результат: появился рабочий endpoint POST /auth/login и защищенный endpoint GET /auth/me, что позволило подключать к backend web, desktop и mobile-клиенты."
        )
    },
    @{
        Date = "08.06."
        Paragraphs = @(
            "Выполнено: реализован сервис работы с заявками. Добавлено создание заявки сотрудником, просмотр заявок, просмотр одной заявки, редактирование администратором или техником и ограничение доступа сотрудника только к своим заявкам.",
            "Результат: сформирована основная бизнес-логика ServiceDesk: пользователь создает обращение, техник или администратор обрабатывает его, меняет статус и назначает дальнейшие действия."
        )
    },
    @{
        Date = "09.06."
        Paragraphs = @(
            "Выполнено: добавлена смена статуса заявки через отдельный endpoint PATCH /tickets/{id}/status. При переводе заявки в RESOLVED или CLOSED автоматически заполняется дата закрытия.",
            "Результат: реализован жизненный цикл заявки от NEW до CLOSED. При смене статуса можно добавить комментарий, чтобы сохранить историю выполненных действий."
        )
    },
    @{
        Date = "10.06."
        Paragraphs = @(
            "Выполнено: реализованы комментарии к заявкам. Созданы DTO CommentRequest и CommentResponse, методы получения списка комментариев и добавления нового сообщения к выбранной заявке.",
            "Результат: у заявки появилась история обсуждения, где фиксируется автор, текст и время комментария. Это делает систему ближе к реальному ServiceDesk-процессу."
        )
    },
    @{
        Date = "11.06."
        Paragraphs = @(
            "Выполнено: разработан модуль учета оборудования. Реализованы создание, просмотр, редактирование и удаление карточек оборудования с инвентарным номером, типом, статусом, местоположением и ответственным сотрудником.",
            "Результат: создан REST API /equipment, через который администратор и техник могут вести реестр техники организации и привязывать оборудование к заявкам."
        )
    },
    @{
        Date = "15.06."
        Paragraphs = @(
            "Выполнено: реализован модуль отчетности. Добавлен ReportService и endpoint GET /reports/dashboard для расчета количества всех заявок, открытых заявок, решенных заявок и оборудования в ремонте.",
            "Результат: администратор получил сводную панель с агрегированными показателями ticketsByStatus и equipmentByStatus, что позволяет быстро оценить состояние технической поддержки."
        )
    },
    @{
        Date = "16.06."
        Paragraphs = @(
            "Выполнено: добавлена обработка ошибок API. Реализованы ApiError, ResourceNotFoundException и общий обработчик исключений, который возвращает понятные JSON-ответы при ошибках валидации, доступа и поиска данных.",
            "Результат: backend стал удобнее для клиентских приложений: вместо технических stack trace возвращаются структурированные сообщения со статусом, временем, текстом ошибки и путем запроса."
        )
    },
    @{
        Date = "17.06."
        Paragraphs = @(
            "Выполнено: подготовлена автоматическая загрузка демонстрационных данных при первом запуске. Созданы тестовые пользователи admin, tech и user, подразделения, оборудование и пример заявки.",
            "Результат: систему можно демонстрировать сразу после запуска без ручного заполнения базы данных. Добавлены роли администратора, техника и сотрудника с готовыми паролями."
        )
    },
    @{
        Date = "18.06."
        Paragraphs = @(
            "Выполнено: разработана web-админка на HTML, CSS и JavaScript. Реализован экран входа, хранение JWT в localStorage, запросы к backend и переключение между разделами «Сводка», «Заявки» и «Оборудование».",
            "Результат: создан пользовательский интерфейс администратора, через который можно войти в систему, посмотреть показатели dashboard, создать заявку и открыть реестр оборудования."
        )
    },
    @{
        Date = "19.06."
        Paragraphs = @(
            "Выполнено: доработан раздел заявок web-админки. Добавлена таблица заявок с ID, темой, приоритетом, статусом, автором и выпадающим списком для смены статуса.",
            "Результат: через браузер реализован полный сценарий обработки обращения: просмотр списка, создание новой заявки, изменение статуса и автоматическое обновление данных после операции."
        )
    },
    @{
        Date = "22.06."
        Paragraphs = @(
            "Выполнено: оформлен раздел оборудования в web-интерфейсе. Карточки оборудования отображают состояние, наименование, инвентарный номер, тип, локацию и ответственного сотрудника.",
            "Результат: администратор может быстро просматривать реестр техники и видеть, какое оборудование находится в эксплуатации или ремонте, что дополняет учет заявок технической поддержки."
        )
    },
    @{
        Date = "23.06."
        Paragraphs = @(
            "Выполнено: разработан desktop-клиент администратора на Kotlin/JVM и Swing. Реализованы окно входа, подключение к REST API, таблица заявок и кнопки обновления данных.",
            "Результат: создано отдельное настольное приложение, которое может использовать администратор или техник без браузера для просмотра заявок и контроля их текущего состояния."
        )
    },
    @{
        Date = "24.06."
        Paragraphs = @(
            "Выполнено: добавлена смена статуса заявки в desktop-приложении. Пользователь выбирает строку в таблице, задает новый статус и отправляет PATCH-запрос на backend.",
            "Результат: desktop-клиент стал не просто просмотрщиком данных, а рабочим инструментом для обработки заявок. Все изменения проходят через защищенный JWT API."
        )
    },
    @{
        Date = "25.06."
        Paragraphs = @(
            "Выполнено: создан Android-проект на Kotlin. Настроены Gradle, AndroidManifest, разрешение INTERNET, базовая тема приложения и главный экран авторизации техника.",
            "Результат: подготовлена мобильная часть ServiceDesk, рассчитанная на запуск в Android Studio. Для обращения к локальному backend на эмуляторе используется адрес http://10.0.2.2:8080/api."
        )
    },
    @{
        Date = "26.06."
        Paragraphs = @(
            "Выполнено: реализован REST-клиент Android-приложения через стандартный HttpURLConnection. Добавлены login-запрос, получение JWT-токена, запрос списка заявок и обработка ошибок.",
            "Результат: мобильное приложение получило связь с backend без сторонних библиотек, что упрощает запуск учебного проекта и показывает понимание базового HTTP-взаимодействия."
        )
    },
    @{
        Date = "29.06."
        Paragraphs = @(
            "Выполнено: разработан экран списка заявок в Android-приложении. Для каждой заявки отображаются номер, тема, приоритет, статус и автор, а также быстрые кнопки «В работу» и «Решено».",
            "Результат: техник может с мобильного устройства просматривать обращения и оперативно менять их статус, что соответствует реальному сценарию выездной или внутренней технической поддержки."
        )
    },
    @{
        Date = "30.06."
        Paragraphs = @(
            "Выполнено: подготовлена контейнеризация проекта. Созданы Dockerfile для backend и frontend, а также docker-compose.yml для запуска PostgreSQL, backend, web-админки и pgAdmin.",
            "Результат: проект можно развернуть одной командой docker compose up --build. Контейнеры связаны между собой, база данных имеет healthcheck, а backend получает настройки через переменные окружения."
        )
    },
    @{
        Date = "01.07."
        Paragraphs = @(
            "Выполнено: добавлены автоматические тесты backend. Реализован JwtServiceTest для проверки генерации и валидации токена, а также ReportServiceTest для проверки агрегирования отчетных показателей.",
            "Результат: ключевая серверная логика получила базовое покрытие JUnit и Mockito. Подготовлен профиль application-test.yml с H2 для тестовой среды."
        )
    },
    @{
        Date = "02.07."
        Paragraphs = @(
            "Выполнено: оформлена проектная документация. Подготовлены техническое задание, руководство пользователя, описание REST API, схема базы данных, UML-диаграммы, план тестирования и отчет по практике.",
            "Результат: проект получил полный комплект сопроводительных материалов, необходимых для защиты: описаны назначение системы, состав модулей, сценарии работы, структура БД и порядок проверки."
        )
    },
    @{
        Date = "03.07."
        Paragraphs = @(
            "Выполнено: проведена итоговая проверка проекта и структуры файлов. Проверены основные настройки безопасности, адреса API, документация, состав модулей и готовность проекта к демонстрации.",
            "Результат: сформирована финальная версия системы Corporate ServiceDesk: backend на Java, web-админка, desktop-клиент на Kotlin, Android-приложение, PostgreSQL, Docker-конфигурация и подробный отчет о выполненных работах."
        )
    }
)

$zip = [System.IO.Compression.ZipFile]::Open($outputPath, [System.IO.Compression.ZipArchiveMode]::Update)
try {
    $entry = $zip.GetEntry("word/document.xml")
    $reader = [System.IO.StreamReader]::new($entry.Open())
    $documentXml = $reader.ReadToEnd()
    $reader.Close()

    [xml]$doc = $documentXml
    $ns = [System.Xml.XmlNamespaceManager]::new($doc.NameTable)
    $ns.AddNamespace("w", $namespace)
    $ns.AddNamespace("r", $relNamespace)

    $tables = $doc.SelectNodes("//w:tbl", $ns)
    if ($tables.Count -lt 2) {
        throw "Expected daily work table was not found."
    }

    $dailyTable = $tables.Item(1)
    $rows = $dailyTable.SelectNodes("./w:tr", $ns)
    foreach ($entryData in $entries) {
        $row = $null
        foreach ($candidate in $rows) {
            $cells = $candidate.SelectNodes("./w:tc", $ns)
            if ($cells.Count -ge 2) {
                $dateText = (($cells.Item(0).SelectNodes(".//w:t", $ns) | ForEach-Object { $_.'#text' }) -join "").Trim()
                if ($dateText -eq $entryData.Date) {
                    $row = $candidate
                    break
                }
            }
        }

        if ($null -eq $row) {
            throw "Daily row not found for date $($entryData.Date)"
        }

        $targetCell = $row.SelectNodes("./w:tc", $ns).Item(1)
        Set-CellParagraphs -Doc $doc -Cell $targetCell -Paragraphs $entryData.Paragraphs
    }

    $entry.Delete()
    $newEntry = $zip.CreateEntry("word/document.xml")
    $writer = [System.IO.StreamWriter]::new($newEntry.Open(), [System.Text.UTF8Encoding]::new($false))
    $doc.Save($writer)
    $writer.Close()
}
finally {
    $zip.Dispose()
}

Write-Output $outputPath
