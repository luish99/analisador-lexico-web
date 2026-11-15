# Analisador Léxico Web

Aplicação web local que utiliza as classes Java do framework `afds.zip` e o autômato `AFD.XML` de `lexico.zip`. A interface permite digitar texto ou carregar um arquivo `.txt`, exibe os tokens reconhecidos, o passo a passo das transições, a tabela e o diagrama do AFD. A análise para no primeiro erro léxico.

## Requisito

Instale um **JDK 17 ou superior**. Verifique no terminal:

```text
java -version
javac -version
```

Os dois comandos precisam funcionar. Se `javac` não for encontrado, adicione o diretório `bin` do JDK ao `PATH` e abra um novo terminal.

## Executar no Windows

1. Clone ou baixe este repositório e abra a pasta `analisador-lexico-web`.
2. Dê duplo clique em `iniciar.cmd` ou execute no PowerShell, **a partir da pasta do projeto**:

   ```powershell
   powershell -ExecutionPolicy Bypass -File .\iniciar.ps1
   ```

3. Abra [http://127.0.0.1:8080](http://127.0.0.1:8080) no navegador.

Também é possível chamar o script pelo caminho completo a partir de qualquer diretório:

```powershell
powershell -ExecutionPolicy Bypass -File "C:\caminho\para\analisador-lexico-web\iniciar.ps1"
```

O caminho relativo `.\iniciar.ps1` procura o arquivo no diretório **atual** do terminal. Por exemplo, não funcionará se o terminal estiver em `C:\WINDOWS\System32`.

## Executar no Linux ou macOS

Na pasta do projeto:

```sh
mkdir -p build
javac -encoding UTF-8 -d build src/afds/*.java src/app/*.java
java -cp build app.Server
```

Depois, abra [http://127.0.0.1:8080](http://127.0.0.1:8080). Para encerrar o servidor, pressione `Ctrl+C` no terminal. O servidor escuta somente em `127.0.0.1`.

## Linguagem reconhecida

O AFD reconhece inteiros `0` ou iniciados de `1` a `9`, e decimais com pelo menos um dígito antes e depois do ponto. Espaços e quebras de linha separam números.

| Válidos | Inválidos |
| --- | --- |
| `0`, `12`, `0.5`, `12.34` | `.5`, `12.`, letras, vírgulas e tabulações |

O fim do arquivo encerra o último número mesmo sem espaço final. O código do framework em `src/afds` foi preservado; `app.Lexer` chama `AFD.p` em cada transição. A interface usa HTML, CSS e JavaScript sem dependências externas.

