param(
    [string]$OutputDir = "diagrams"
)

$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Drawing

$rootDir = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$diagramDir = Join-Path $rootDir $OutputDir
New-Item -ItemType Directory -Path $diagramDir -Force | Out-Null

function Color-Hex([string]$hex) {
    return [System.Drawing.ColorTranslator]::FromHtml($hex)
}

function New-Font([float]$size, [System.Drawing.FontStyle]$style = [System.Drawing.FontStyle]::Regular) {
    return [System.Drawing.Font]::new("Segoe UI", $size, $style, [System.Drawing.GraphicsUnit]::Point)
}

function Draw-Title($g, [string]$title, [string]$subtitle = "") {
    $titleFont = New-Font 32 ([System.Drawing.FontStyle]::Bold)
    $subFont = New-Font 17
    $brush = [System.Drawing.SolidBrush]::new((Color-Hex "#17211B"))
    $muted = [System.Drawing.SolidBrush]::new((Color-Hex "#52665A"))
    $format = [System.Drawing.StringFormat]::new()
    $format.Alignment = [System.Drawing.StringAlignment]::Center
    $g.DrawString($title, $titleFont, $brush, [System.Drawing.RectangleF]::new(0, 26, 1600, 48), $format)
    if ($subtitle.Length -gt 0) {
        $g.DrawString($subtitle, $subFont, $muted, [System.Drawing.RectangleF]::new(0, 78, 1600, 34), $format)
    }
    $titleFont.Dispose()
    $subFont.Dispose()
    $brush.Dispose()
    $muted.Dispose()
    $format.Dispose()
}

function Draw-Box($g, [float]$x, [float]$y, [float]$w, [float]$h, [string]$title, [string[]]$lines, [string]$fill, [string]$border = "#315242") {
    $rect = [System.Drawing.RectangleF]::new($x, $y, $w, $h)
    $fillBrush = [System.Drawing.SolidBrush]::new((Color-Hex $fill))
    $borderPen = [System.Drawing.Pen]::new((Color-Hex $border), 3)
    $g.FillRectangle($fillBrush, $rect)
    $g.DrawRectangle($borderPen, $x, $y, $w, $h)

    $titleFont = New-Font 18 ([System.Drawing.FontStyle]::Bold)
    $lineFont = New-Font 14
    $titleBrush = [System.Drawing.SolidBrush]::new((Color-Hex "#17211B"))
    $lineBrush = [System.Drawing.SolidBrush]::new((Color-Hex "#26352D"))
    $g.DrawString($title, $titleFont, $titleBrush, [System.Drawing.RectangleF]::new($x + 18, $y + 14, $w - 36, 28))

    $yy = $y + 50
    foreach ($line in $lines) {
        $g.DrawString($line, $lineFont, $lineBrush, [System.Drawing.RectangleF]::new($x + 18, $yy, $w - 36, 24))
        $yy += 24
    }

    $fillBrush.Dispose()
    $borderPen.Dispose()
    $titleFont.Dispose()
    $lineFont.Dispose()
    $titleBrush.Dispose()
    $lineBrush.Dispose()
}

function Draw-Arrow($g, [float]$x1, [float]$y1, [float]$x2, [float]$y2, [string]$label = "", [string]$color = "#315242") {
    $pen = [System.Drawing.Pen]::new((Color-Hex $color), 4)
    $cap = [System.Drawing.Drawing2D.AdjustableArrowCap]::new(7, 9)
    $pen.CustomEndCap = $cap
    $g.DrawLine($pen, $x1, $y1, $x2, $y2)

    if ($label.Length -gt 0) {
        $font = New-Font 13 ([System.Drawing.FontStyle]::Bold)
        $brush = [System.Drawing.SolidBrush]::new((Color-Hex "#17211B"))
        $bg = [System.Drawing.SolidBrush]::new((Color-Hex "#FFF8EA"))
        $mx = ($x1 + $x2) / 2
        $my = ($y1 + $y2) / 2
        $size = $g.MeasureString($label, $font)
        $g.FillRectangle($bg, $mx - ($size.Width / 2) - 8, $my - 16, $size.Width + 16, 28)
        $g.DrawString($label, $font, $brush, $mx - ($size.Width / 2), $my - 14)
        $font.Dispose()
        $brush.Dispose()
        $bg.Dispose()
    }

    $cap.Dispose()
    $pen.Dispose()
}

function Draw-Note($g, [float]$x, [float]$y, [string]$text) {
    $font = New-Font 15 ([System.Drawing.FontStyle]::Italic)
    $brush = [System.Drawing.SolidBrush]::new((Color-Hex "#52665A"))
    $g.DrawString($text, $font, $brush, [System.Drawing.RectangleF]::new($x, $y, 1450, 60))
    $font.Dispose()
    $brush.Dispose()
}

function Save-Diagram([string]$fileName, [scriptblock]$draw) {
    $path = Join-Path $diagramDir $fileName
    $bmp = [System.Drawing.Bitmap]::new(1600, 1100)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
    $g.Clear((Color-Hex "#FFF8EA"))
    & $draw $g
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose()
    $bmp.Dispose()
    Write-Output $path
}

Save-Diagram "01_er_diagram.png" {
    param($g)
    Draw-Title $g "ER-диаграмма Corporate ServiceDesk" "Основные сущности и связи информационной системы"
    Draw-Box $g 80 190 330 220 "USERS" @("id PK", "username", "password_hash", "full_name", "email", "role") "#E7F3E7"
    Draw-Box $g 620 170 370 260 "TICKETS" @("id PK", "title", "description", "priority", "status", "equipment_id FK", "created_by_id FK", "assigned_to_id FK") "#F9E7D7"
    Draw-Box $g 1180 190 340 220 "EQUIPMENT" @("id PK", "inventory_number", "title", "type", "status", "assigned_to_id FK", "department_id FK") "#E6EEF8"
    Draw-Box $g 620 700 390 210 "TICKET_COMMENTS" @("id PK", "ticket_id FK", "author_id FK", "message", "created_at") "#EDE4F2"
    Draw-Box $g 1180 720 340 170 "DEPARTMENTS" @("id PK", "name", "location") "#F5F1D4"

    Draw-Arrow $g 410 255 620 255 "created_by 1:N"
    Draw-Arrow $g 410 330 620 330 "assigned_to 1:N"
    Draw-Arrow $g 1180 300 990 300 "equipment_id N:1"
    Draw-Arrow $g 815 430 815 700 "comments 1:N"
    Draw-Arrow $g 410 410 620 780 "author 1:N"
    Draw-Arrow $g 1350 720 1350 410 "department 1:N"

    Draw-Note $g 90 990 "Связи показывают, что один пользователь может создать много заявок, одна заявка может иметь много комментариев, а оборудование может быть связано с подразделением и обращениями."
}

Save-Diagram "02_application_architecture.png" {
    param($g)
    Draw-Title $g "Схема приложения" "Клиентские интерфейсы, backend, безопасность, база данных и Docker-инфраструктура"
    Draw-Box $g 70 190 310 150 "WEB-АДМИНКА" @("HTML / CSS / JS", "вход, заявки", "оборудование, отчеты") "#E7F3E7"
    Draw-Box $g 70 440 310 150 "DESKTOP ADMIN" @("Kotlin / Swing", "таблица заявок", "смена статуса") "#E7F3E7"
    Draw-Box $g 70 690 310 150 "ANDROID MOBILE" @("Kotlin", "заявки техника", "быстрые действия") "#E7F3E7"

    Draw-Box $g 560 250 360 210 "SPRING BOOT REST API" @("AuthController", "TicketController", "EquipmentController", "ReportController") "#F9E7D7"
    Draw-Box $g 560 570 360 180 "SECURITY + JWT" @("Spring Security", "JwtAuthenticationFilter", "проверка ролей") "#F5F1D4"
    Draw-Box $g 1060 220 390 220 "БИЗНЕС-СЛОЙ" @("TicketService", "EquipmentService", "ReportService", "UserService") "#E6EEF8"
    Draw-Box $g 1060 570 390 190 "POSTGRESQL" @("users", "tickets", "equipment", "ticket_comments") "#EDE4F2"
    Draw-Box $g 1060 830 390 120 "DOCKER COMPOSE" @("backend + frontend", "postgres + pgAdmin") "#F5F1D4"

    Draw-Arrow $g 380 265 560 325 "HTTPS/JSON"
    Draw-Arrow $g 380 515 560 370 "REST"
    Draw-Arrow $g 380 765 560 420 "REST"
    Draw-Arrow $g 740 460 740 570 "Bearer JWT"
    Draw-Arrow $g 920 350 1060 330 "DTO"
    Draw-Arrow $g 1240 440 1240 570 "JPA"
    Draw-Arrow $g 1255 760 1255 830 "контейнеры"

    Draw-Note $g 90 1000 "Все клиенты работают через единый защищенный REST API. Backend изолирует бизнес-логику от интерфейсов и хранит данные в PostgreSQL."
}

Save-Diagram "03_database_schema.png" {
    param($g)
    Draw-Title $g "Схема базы данных PostgreSQL" "Таблицы, ключевые поля и внешние ключи"
    Draw-Box $g 70 170 360 300 "users" @("id BIGSERIAL PK", "username VARCHAR UNIQUE", "password VARCHAR", "full_name VARCHAR", "email VARCHAR UNIQUE", "role VARCHAR", "enabled BOOLEAN") "#E7F3E7"
    Draw-Box $g 610 150 420 350 "tickets" @("id BIGSERIAL PK", "title VARCHAR", "description VARCHAR(4000)", "priority VARCHAR", "status VARCHAR", "equipment_id BIGINT FK", "created_by_id BIGINT FK", "assigned_to_id BIGINT FK", "created_at TIMESTAMP", "updated_at TIMESTAMP") "#F9E7D7"
    Draw-Box $g 1160 170 360 300 "equipment" @("id BIGSERIAL PK", "inventory_number UNIQUE", "title VARCHAR", "type VARCHAR", "status VARCHAR", "location VARCHAR", "assigned_to_id BIGINT FK", "department_id BIGINT FK") "#E6EEF8"
    Draw-Box $g 610 700 430 230 "ticket_comments" @("id BIGSERIAL PK", "ticket_id BIGINT FK", "author_id BIGINT FK", "message VARCHAR(2000)", "created_at TIMESTAMP") "#EDE4F2"
    Draw-Box $g 1160 720 360 190 "departments" @("id BIGSERIAL PK", "name VARCHAR UNIQUE", "location VARCHAR") "#F5F1D4"

    Draw-Box $g 70 535 470 120 "FK-связи tickets" @("created_by_id -> users.id", "assigned_to_id -> users.id", "equipment_id -> equipment.id") "#FFFFFF" "#C46A3A"
    Draw-Arrow $g 800 500 800 700 "ticket_id"
    Draw-Arrow $g 430 440 610 790 "author_id"
    Draw-Arrow $g 1350 720 1350 470 "department_id"

    Draw-Note $g 90 990 "Схема нормализована: пользователи, заявки, оборудование и комментарии хранятся отдельно, а связи задаются внешними ключами."
}

Save-Diagram "04_internal_interaction.png" {
    param($g)
    Draw-Title $g "Схема взаимодействия внутри приложения" "Типовой сценарий: вход, проверка токена, обработка заявки и запись в БД"

    Draw-Box $g 55 190 220 120 "ПОЛЬЗОВАТЕЛЬ" @("администратор", "техник", "сотрудник") "#E7F3E7"
    Draw-Box $g 320 190 210 120 "CLIENT UI" @("web", "desktop", "mobile") "#E7F3E7"
    Draw-Box $g 575 190 230 120 "AUTH API" @("POST /auth/login", "выдача JWT") "#F9E7D7"
    Draw-Box $g 850 190 220 120 "JWT FILTER" @("проверка токена", "роль пользователя") "#F5F1D4"
    Draw-Box $g 1120 190 220 120 "CONTROLLER" @("tickets", "equipment", "reports") "#F9E7D7"
    Draw-Box $g 545 610 230 120 "SERVICE" @("бизнес-логика", "права доступа") "#E6EEF8"
    Draw-Box $g 825 610 220 120 "REPOSITORY" @("Spring Data JPA", "запросы") "#E6EEF8"
    Draw-Box $g 1095 610 220 120 "DATABASE" @("PostgreSQL", "таблицы") "#EDE4F2"

    Draw-Arrow $g 275 250 320 250 "1"
    Draw-Arrow $g 530 250 575 250 "2 login"
    Draw-Arrow $g 805 250 850 250 "3 token"
    Draw-Arrow $g 1070 250 1120 250 "4 request"
    Draw-Arrow $g 1230 310 660 610 "5 DTO"
    Draw-Arrow $g 775 670 825 670 "6"
    Draw-Arrow $g 1045 670 1095 670 "7 SQL"
    Draw-Arrow $g 1095 720 775 720 "8 data"
    Draw-Arrow $g 545 650 410 310 "9 JSON"

    $font = New-Font 18 ([System.Drawing.FontStyle]::Bold)
    $brush = [System.Drawing.SolidBrush]::new((Color-Hex "#315242"))
    $g.DrawString("Последовательность:", $font, $brush, 80, 400)
    $font.Dispose()
    $brush.Dispose()

    Draw-Box $g 80 450 1380 110 "ШАГИ" @(
        "1) пользователь выполняет действие в интерфейсе; 2) клиент отправляет REST-запрос; 3) backend проверяет JWT;",
        "4) controller принимает DTO; 5) service выполняет бизнес-логику; 6) repository читает/пишет данные; 7) клиент получает JSON-ответ."
    ) "#FFFFFF" "#C46A3A"

    Draw-Note $g 90 970 "Такая схема разделяет ответственность: интерфейс отображает данные, controller принимает запрос, service выполняет правила, repository работает с БД."
}
