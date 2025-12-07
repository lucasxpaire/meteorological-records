# Sistema de Registros Meteorológicos

Este é um sistema web desenvolvido em Java para gerenciamento, visualização e análise de dados meteorológicos. O projeto permite o cadastro de propriedades, proprietários e a visualização de dados climáticos em mapas, utilizando dados históricos de estações meteorológicas.

## 📁 Sobre os Dados Meteorológicos (Importante)

Para o correto funcionamento das análises históricas, o sistema espera processar arquivos CSV contendo registros climáticos.

**Atenção:** A pasta `resources/dadosEstacoesMeteorologicas` não está incluída neste repositório devido ao tamanho dos arquivos.

Para obter os dados necessários:
1. Acesse o portal do INMET: **[https://portal.inmet.gov.br/dadoshistoricos](https://portal.inmet.gov.br/dadoshistoricos)**
2. Baixe os anos de interesse.
3. Extraia os arquivos `.csv` e coloque-os dentro da pasta `resources/dadosEstacoesMeteorologicas` (crie esta pasta se ela não existir dentro de `resources`).

> O projeto já contém alguns exemplos de CSV na raiz de `resources` (ex: `SantaMaria.csv`, `PortoAlegre.csv`) para testes rápidos, mas a carga completa depende do passo acima.

## 🚀 Tecnologias Utilizadas

O projeto foi construído utilizando uma arquitetura MVC clássica com as seguintes tecnologias:

* **Linguagem:** Java
* **Framework Web:** Spring MVC (v4.3.30)
* **Persistência:** Hibernate ORM / JPA (v4.3.11)
* **Banco de Dados:** MySQL (Connector v8.0.27)
* **Frontend:** JSP (JavaServer Pages), JSTL, CSS3 e JavaScript.
* **Análise de Dados/Matemática:**
    * Apache Commons Math3
    * Smile (Statistical Machine Intelligence and Learning Engine)
    * Timeseries Forecast
* **Geospacial:**
    * Timeshape (GeoJSON)
    * Esri Geometry API

## ⚙️ Configuração do Ambiente

### Pré-requisitos
* Java JDK 8 ou superior.
* Servidor de Aplicação (Apache Tomcat recomendado).
* Banco de Dados MySQL instalado e rodando.

### Configuração do Banco de Dados
1. Crie um banco de dados no MySQL (o nome padrão geralmente é `meteorological_records`, mas verifique sua configuração).
2. Verifique o arquivo de persistência em:
   `src/META-INF/persistence.xml`
3. Ajuste as propriedades `javax.persistence.jdbc.user`, `javax.persistence.jdbc.password` e `javax.persistence.jdbc.url` conforme as credenciais do seu banco local.

### Dependências
Este projeto não utiliza gerenciadores como Maven ou Gradle. Todas as bibliotecas necessárias (`.jar`) encontram-se na pasta:
* `lib/`

Certifique-se de adicionar todos os JARs desta pasta ao **Classpath** ou às **Libraries** do seu projeto na sua IDE (IntelliJ, Eclipse, NetBeans).

## 🏗️ Estrutura do Projeto

* **src/modelo**: Classes de entidade (POJOs) como `EstacaoMeteorologica`, `RegistroMeteorologico`, `Propriedade`.
* **src/servico**: Regras de negócio e comunicação entre controllers e repositórios.
* **src/web/controller**: Controladores do Spring MVC (`MapaController`, `ProprietarioController`, etc.).
* **src/web/json**: Utilitários para serialização de dados para o frontend.
* **web/WEB-INF/jsp**: Páginas de visualização do sistema.
* **web/js**: Scripts para manipulação de mapas e formulários.

## ▶️ Como Executar

1. Importe o projeto na sua IDE.
2. Configure o servidor Tomcat e adicione o artefato do projeto (WAR ou exploded WAR).
3. Inicie o servidor.
4. Acesse no navegador: `http://localhost:8080/` (ou a porta configurada no seu Tomcat).

---
*Projeto desenvolvido como parte de atividades acadêmicas/estágio (Pré-Estágio 2022).*
