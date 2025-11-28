@echo off
echo ========================================
echo GERADOR DE HEMOGRAMAS FICTICIOS
echo ========================================
echo.

REM Verifica se Python está instalado
python --version >nul 2>&1
if errorlevel 1 (
    echo ERRO: Python nao encontrado!
    echo Instale Python 3.x de https://www.python.org/
    pause
    exit /b 1
)

REM Instala requests se necessário
echo Verificando dependencias...
python -c "import requests" >nul 2>&1
if errorlevel 1 (
    echo Instalando biblioteca requests...
    pip install requests
)

echo.
echo Executando gerador...
echo.

python "%~dp0gerar-hemogramas-ficticios.py"

echo.
pause

