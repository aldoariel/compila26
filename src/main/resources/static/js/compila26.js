const compileForm = document.querySelector("#compile-form");
const sourceCode = document.querySelector("#source-code");
const compileButton = document.querySelector("#compile-button");
const compileStatus = document.querySelector("#compile-status");
const resultMessage = document.querySelector("#result-message");

const lexicalErrors = document.querySelector("#lexical-errors");
const syntaxErrors = document.querySelector("#syntax-errors");
const semanticErrors = document.querySelector("#semantic-errors");
const tokensContent = document.querySelector("#tokens-content");
const astContent = document.querySelector("#ast-content");
const symbolsContent = document.querySelector("#symbols-content");
const intermediateContent = document.querySelector("#intermediate-content");
const neanderDiagnostics = document.querySelector("#neander-diagnostics");
const neanderContent = document.querySelector("#neander-content");
const neanderData = document.querySelector("#neander-data");
const modal = document.querySelector("#didactic-modal");
const modalDialog = modal.querySelector(".modal-dialog");
const modalTitle = document.querySelector("#modal-title");
const modalContent = document.querySelector("#modal-content");
let previousFocus = null;

const phaseHelp = {
    lexical: ["Análisis léxico", "Convierte el código fuente en una secuencia de tokens."],
    syntax: ["Análisis sintáctico", "Comprueba la estructura gramatical y construye el AST."],
    semantic: ["Análisis semántico", "Comprueba reglas relacionadas con significado, declaraciones y tipos según las capacidades actuales de Compila26."],
    intermediate: ["Código intermedio", "Transforma el AST válido en una representación intermedia más cercana a una futura generación de código."],
    "intermediate-detail": ["Código de tres direcciones", "Este es código intermedio de tres direcciones. Los temporales t0, t1, ... representan resultados intermedios utilizados durante la traducción."],
    neander: ["Backend NEANDER", "NEANDER es la máquina objetivo didáctica adoptada. Su backend recibe código intermedio y selecciona instrucciones para una arquitectura basada en acumulador y memoria."],
    "neander-detail": ["Código objetivo / NEANDER", "Las variables, constantes y temporales se asignan a memoria. Una instrucción intermedia puede necesitar varias instrucciones NEANDER; la representación intermedia permanece independiente de esta máquina."]
};

const curricularHelp = {
    compiler: ["Compilador", "Programa que analiza un lenguaje fuente y lo traduce a otra representación, preservando su significado."],
    interpreter: ["Intérprete", "Procesador de lenguaje que analiza y ejecuta el programa durante su funcionamiento, normalmente sin producir antes un programa objetivo completo."],
    analysis: ["Análisis", "Parte del compilador que reconoce los componentes del código, comprueba su estructura y significado, y construye una representación interna."],
    synthesis: ["Síntesis", "Parte que transforma la representación interna en código intermedio y, posteriormente, en código objetivo."],
    source: ["Código fuente", "Texto escrito en el lenguaje que recibe el compilador: declaraciones, asignaciones y expresiones aritméticas."],
    lexical: phaseHelp.lexical,
    tokens: ["Tokens", "Unidades léxicas clasificadas, como palabras reservadas, identificadores, números, operadores y delimitadores."],
    syntax: phaseHelp.syntax,
    ast: ["AST", "Árbol de sintaxis abstracta que conserva la estructura esencial del programa sin representar cada detalle textual."],
    semantic: phaseHelp.semantic,
    symbols: ["Tabla de símbolos", "Registra los identificadores declarados y su tipo para comprobar posteriormente sus usos."],
    intermediate: phaseHelp.intermediate,
    target: ["Código objetivo", "Salida destinada a una arquitectura concreta. Compila26 genera actualmente un subconjunto ensamblador para la máquina didáctica NEANDER."],
    bnf: ["BNF y EBNF", "Notaciones para describir gramáticas mediante símbolos, producciones y alternativas. EBNF permite expresar repeticiones de forma compacta."],
    nonterminal: ["Símbolo no terminal", "Categoría de la gramática que se desarrolla mediante producciones; por ejemplo, expresion."],
    terminal: ["Símbolo terminal", "Elemento concreto reconocido en la entrada; por ejemplo, la palabra int o el operador +."],
    production: ["Producción", "Regla que indica cómo se construye un no terminal. Por ejemplo: expresion → suma."],
    alternative: ["Alternativa", "El símbolo | indica elecciones posibles. Una sentencia puede ser una declaración o una asignación."],
    repetition: ["Repetición", "El símbolo * indica cero o más apariciones. Un programa puede contener varias sentencias."],
    regex: ["Expresión regular", "Descripción compacta de un patrón léxico. El Lexer la utiliza para distinguir sus categorías de tokens."],
    automaton: ["Autómata finito", "Modelo de estados y transiciones capaz de reconocer cadenas que corresponden a un patrón regular."],
    nfa: ["AFN", "Autómata finito no determinista: desde un estado puede ofrecer varias posibilidades de transición para una entrada."],
    dfa: ["AFD", "Autómata finito determinista: para cada estado y símbolo de entrada existe como máximo una transición posible."]
};

Object.assign(curricularHelp, {
    "runtime-memory": ["Memoria en tiempo de ejecución", "Al ejecutar un programa, sus instrucciones y datos necesitan organizarse en memoria. Esta vista es conceptual y no asigna direcciones reales."],
    "memory-code": ["Región de código", "Contiene las instrucciones del programa. Compila26 todavía no genera ni ubica código objetivo en una memoria física."],
    "static-data": ["Datos estáticos", "Región asociada conceptualmente a datos cuya ubicación puede determinarse antes de la ejecución."],
    stack: ["Stack / Pila", "Región usada normalmente para llamadas, parámetros, variables locales y control de ejecución. Compila26 todavía no posee funciones ni activaciones reales."],
    heap: ["Heap / Montículo", "Región utilizada normalmente para memoria solicitada dinámicamente. El lenguaje actual de Compila26 no implementa asignación dinámica."],
    "compile-time": ["Tiempo de compilación", "Momento en que Lexer, Parser y análisis semántico estudian el programa y producen representaciones como AST, tabla de símbolos y código intermedio."],
    allocation: ["Asignación de memoria", "Estrategia utilizada para decidir cuándo y cómo se reserva almacenamiento para la información necesaria durante la ejecución."],
    "static-allocation": ["Asignación estática", "La ubicación de los datos se determina antes de ejecutar. Es adecuada conceptualmente cuando sus necesidades son conocidas de antemano."],
    "stack-allocation": ["Asignación en pila", "Reserva y libera memoria siguiendo normalmente una disciplina LIFO: el último elemento incorporado es el primero en retirarse."],
    "dynamic-allocation": ["Asignación dinámica / Heap", "La memoria se solicita y libera durante la ejecución según las necesidades del programa."]
});

Object.assign(curricularHelp, {
    ir: ["Representación intermedia", "Modelo interno que conecta el análisis del programa con una futura generación de código objetivo, sin depender de una máquina concreta."],
    "three-address": ["Código de tres direcciones", "Secuencia lineal de instrucciones simples. Cada operación combina operandos y guarda su resultado en una variable o temporal."],
    temporary: ["Variable temporal", "Nombre generado automáticamente, como t0 o t1, que conserva el resultado intermedio de una expresión durante la traducción."],
    operand: ["Operando", "Valor utilizado por una operación. En Compila26 puede ser una constante, una variable o un temporal."],
    operator: ["Operador", "Indica la operación que debe realizarse, como sumar, restar, multiplicar o dividir."],
    "ir-result": ["Resultado", "Destino donde queda almacenado conceptualmente el resultado de la instrucción intermedia."],
    declaration: ["Declaración", "Introduce un identificador y su tipo en la tabla de símbolos. En el modelo actual no produce por sí sola código de tres direcciones."],
    assignment: ["Asignación", "Asocia el resultado de una expresión con una variable previamente declarada y sí produce una instrucción intermedia."]
});

Object.assign(curricularHelp, {
    "target-code": ["Código objetivo", "Código adaptado a una arquitectura concreta. El backend didáctico actual genera ensamblador NEANDER para asignación, suma y resta."],
    "abstract-location": ["Ubicación abstracta", "Nombre didáctico como M0 que representa una posible ubicación sin convertirse todavía en una dirección física ni modificar la tabla de símbolos."],
    addressing: ["Direccionamiento", "Decisión sobre cómo una instrucción puede localizar o acceder a un dato. En esta parte solo se representa mediante ubicaciones abstractas."],
    "instruction-selection": ["Selección de instrucciones", "Proceso que elige instrucciones de la máquina objetivo para implementar cada operación intermedia. Una operación puede requerir una o varias instrucciones."],
    "code-generation": ["Generación de código", "Traduce la representación intermedia según las características de una máquina objetivo: instrucciones, almacenamiento y formas de acceso."],
    "target-machine": ["Máquina objetivo", "Arquitectura para la cual se produce el código final. Compila26 adopta NEANDER como máquina objetivo didáctica."],
    optimization: ["Optimización", "Busca mejorar alguna característica del código, como cantidad de instrucciones o uso de recursos, sin modificar el significado del programa."],
    "constant-folding": ["Plegado de constantes", "Optimización conceptual que calcula durante compilación expresiones formadas solo por constantes. Compila26 todavía no la implementa."]
});

const tokenExplanations = {
    IDENTIFICADOR: "Representa el nombre de una variable u otro elemento identificable del lenguaje.",
    PALABRA_CLAVE: "Palabra reservada que posee un significado especial dentro del lenguaje.",
    NUMERO: "Representa una constante numérica reconocida por el analizador léxico.",
    OPERADOR: "Símbolo utilizado para realizar una operación o establecer una relación.",
    DELIMITADOR: "Símbolo que ayuda a delimitar la estructura del programa.",
    ERROR: "Secuencia que el analizador léxico no pudo reconocer correctamente."
};

const astExplanations = {
    PROGRAMA: "Nodo raíz del programa analizado.",
    DECLARACION: "Representa la declaración explícita de una variable y su tipo.",
    ASIGNACION: "Representa una sentencia de asignación.",
    ID: "Representa un identificador dentro del árbol.",
    NUMERO: "Representa un literal numérico.",
    OPERADOR: "Representa una operación dentro de una expresión.",
    ERROR: "Representa una parte del árbol que el parser no pudo construir correctamente."
};

document.querySelectorAll(".example-button").forEach((button) => {
    button.addEventListener("click", () => {
        sourceCode.value = button.dataset.code;
        sourceCode.focus();
    });
});

document.querySelector("#try-ir-example")?.addEventListener("click", (event) => {
    sourceCode.value = event.currentTarget.dataset.code;
});

document.querySelector("#try-codegen-example")?.addEventListener("click", (event) => {
    sourceCode.value = event.currentTarget.dataset.code;
});

document.querySelectorAll(".tab").forEach((tab) => {
    tab.addEventListener("click", () => activateTab(tab));
});

document.querySelectorAll("[data-help]").forEach((button) => {
    button.addEventListener("click", () => {
        const [title, explanation] = phaseHelp[button.dataset.help];
        openModal(title, createParagraph(explanation));
    });
});

document.querySelectorAll("[data-topic]").forEach((button) => {
    button.addEventListener("click", () => {
        const [title, explanation] = curricularHelp[button.dataset.topic];
        openModal(title, createParagraph(explanation));
    });
});

document.querySelectorAll("[data-modal-close]").forEach((element) => {
    element.addEventListener("click", closeModal);
});

document.addEventListener("keydown", (event) => {
    if (event.key === "Escape" && !modal.hidden) {
        closeModal();
    }
});

compileForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    setLoading(true);

    try {
        const response = await fetch("/api/compiler/compile", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify({codigo: sourceCode.value})
        });

        if (!response.ok) {
            throw new Error(`El servidor respondió con HTTP ${response.status}.`);
        }

        const contentType = response.headers.get("content-type") || "";
        if (!contentType.includes("application/json")) {
            throw new Error("El servidor devolvió una respuesta inesperada.");
        }

        renderCompilation(await response.json());
    } catch (error) {
        renderConnectionError(error);
    } finally {
        setLoading(false);
    }
});

function activateTab(selectedTab) {
    document.querySelectorAll(".tab").forEach((tab) => {
        const active = tab === selectedTab;
        tab.classList.toggle("active", active);
        tab.setAttribute("aria-selected", active.toString());
    });

    document.querySelectorAll(".tab-panel").forEach((panel) => {
        const active = panel.id === selectedTab.dataset.panel;
        panel.classList.toggle("active", active);
        panel.hidden = !active;
    });
}

function setLoading(loading) {
    compileButton.disabled = loading;
    compileButton.querySelector("span").textContent = loading ? "Compilando…" : "Compilar";

    if (loading) {
        setStatus("status-idle", "Procesando fases…");
        resultMessage.textContent = "Analizando el código fuente.";
        resetPipeline();
    }
}

function renderCompilation(result) {
    const successful = Boolean(result.exitoso);
    const targetLimited = successful && result.neander?.ejecutado && !result.neander.exitoso;
    setStatus(
        targetLimited ? "status-warning" : successful ? "status-success" : "status-error",
        targetLimited
            ? "! Programa válido · limitación NEANDER"
            : successful ? "✓ Compilación exitosa" : "✗ Compilación con errores"
    );
    resultMessage.textContent = result.mensaje || "Compilación finalizada.";
    renderPipeline(result);

    renderErrors(lexicalErrors, result.erroresLexicos, "Errores léxicos", "Léxico");
    renderTokens(result.tokens, result.erroresLexicos);

    renderErrors(syntaxErrors, result.erroresSintacticos, "Errores sintácticos", "Sintáctico");
    renderAst(result.ast, result.erroresLexicos);

    renderSemantic(
        result.erroresSemanticos,
        result.simbolos,
        result.ast,
        result.erroresLexicos,
        result.erroresSintacticos
    );
    renderIntermediate(result.codigoIntermedio, result.erroresLexicos,
        result.erroresSintacticos, result.erroresSemanticos);
    renderNeander(result.neander, result.erroresLexicos,
        result.erroresSintacticos, result.erroresSemanticos);
}

function renderTokens(tokens = [], errors = []) {
    tokensContent.replaceChildren();
    tokensContent.className = "";

    if (tokens.length === 0) {
        showEmpty(tokensContent, errors.length ? "No se recibieron tokens." : "No hay tokens para mostrar.");
        return;
    }

    const wrapper = document.createElement("div");
    wrapper.className = "table-wrap";
    const table = document.createElement("table");
    const header = document.createElement("thead");
    const headerRow = document.createElement("tr");

    ["#", "Tipo", "Valor", "Línea", "Columna"].forEach((label) => {
        const cell = document.createElement("th");
        cell.textContent = label;
        headerRow.appendChild(cell);
    });

    header.appendChild(headerRow);
    table.appendChild(header);

    const body = document.createElement("tbody");
    tokens.forEach((token, index) => {
        const row = document.createElement("tr");
        row.className = "token-row";
        row.tabIndex = 0;
        row.setAttribute("role", "button");
        row.setAttribute("aria-label", `Ver detalle del token ${token.valor}`);
        row.addEventListener("click", () => showTokenDetail(token));
        row.addEventListener("keydown", (event) => {
            if (event.key === "Enter" || event.key === " ") {
                event.preventDefault();
                showTokenDetail(token);
            }
        });
        [index + 1, token.tipo, token.valor, token.linea, token.columna].forEach((value) => {
            const cell = document.createElement("td");
            cell.textContent = value;
            row.appendChild(cell);
        });
        body.appendChild(row);
    });

    table.appendChild(body);
    wrapper.appendChild(table);
    tokensContent.appendChild(wrapper);
}

function renderAst(ast, lexicalErrorList = []) {
    astContent.replaceChildren();
    astContent.className = "";

    if (!ast) {
        const message = lexicalErrorList.length
            ? "AST no disponible: el pipeline se detuvo en el análisis léxico."
            : "No se recibió un AST.";
        showEmpty(astContent, message);
        return;
    }

    const tree = document.createElement("ul");
    tree.className = "ast-tree";
    tree.appendChild(createAstNode(ast));
    astContent.appendChild(tree);
}

function createAstNode(node) {
    const item = document.createElement("li");
    const button = document.createElement("button");
    button.type = "button";
    button.className = "ast-node-button";
    button.textContent = node.tipo || "NODO";
    button.addEventListener("click", () => showAstDetail(node));
    item.appendChild(button);

    if (node.valor !== null && node.valor !== undefined && node.valor !== "") {
        const value = document.createElement("span");
        value.className = "ast-value";
        value.textContent = `: ${node.valor}`;
        button.appendChild(value);
    }

    if (Array.isArray(node.hijos) && node.hijos.length > 0) {
        const children = document.createElement("ul");
        node.hijos.forEach((child) => children.appendChild(createAstNode(child)));
        item.appendChild(children);
    }

    return item;
}

function renderSemantic(errors = [], symbols = [], ast, lexical = [], syntax = []) {
    semanticErrors.replaceChildren();
    semanticErrors.className = "";
    symbolsContent.replaceChildren();
    symbolsContent.className = "";

    const phaseNotExecuted = !ast || lexical.length > 0 || syntax.length > 0;

    if (phaseNotExecuted) {
        showEmpty(semanticErrors, "Análisis semántico no ejecutado.");
    } else if (errors.length === 0) {
        const note = document.createElement("div");
        note.className = "success-note";
        note.textContent = "Sin errores semánticos.";
        semanticErrors.appendChild(note);
    } else {
        semanticErrors.appendChild(createErrorBox(errors, "Errores semánticos", "Semántico"));
    }

    if (symbols.length === 0) {
        showEmpty(symbolsContent, phaseNotExecuted ? "Tabla no disponible." : "La tabla de símbolos está vacía.");
        return;
    }

    const wrapper = document.createElement("div");
    wrapper.className = "table-wrap";
    const table = document.createElement("table");
    table.innerHTML = "<thead><tr><th>Nombre</th><th>Tipo</th></tr></thead>";
    const body = document.createElement("tbody");

    symbols.forEach((symbol) => {
        const row = document.createElement("tr");
        [symbol.nombre, symbol.tipo].forEach((value) => {
            const cell = document.createElement("td");
            cell.textContent = value;
            row.appendChild(cell);
        });
        body.appendChild(row);
    });

    table.appendChild(body);
    wrapper.appendChild(table);
    symbolsContent.appendChild(wrapper);
}

function renderIntermediate(instructions = [], lexical = [], syntax = [], semantic = []) {
    intermediateContent.replaceChildren();
    intermediateContent.className = "";

    if (instructions.length === 0) {
        let message = "No se generaron instrucciones.";
        if (lexical.length) message = "No generado: existen errores léxicos.";
        else if (syntax.length) message = "No generado: existen errores sintácticos.";
        else if (semantic.length) message = "No generado: existen errores semánticos.";
        showEmpty(intermediateContent, message);
        return;
    }

    const code = document.createElement("pre");
    code.className = "instruction-list";
    code.textContent = instructions.join("\n");
    intermediateContent.appendChild(code);
}

function renderNeander(neander, lexical = [], syntax = [], semantic = []) {
    neanderDiagnostics.replaceChildren();
    neanderContent.replaceChildren();
    neanderContent.className = "";
    neanderData.replaceChildren();
    neanderData.className = "";

    if (!neander?.ejecutado) {
        let message = "Esta fase no se ejecutó.";
        if (lexical.length) message = "No ejecutado: existen errores léxicos.";
        else if (syntax.length) message = "No ejecutado: existen errores sintácticos.";
        else if (semantic.length) message = "No ejecutado: existen errores semánticos.";
        showEmpty(neanderContent, message);
        showEmpty(neanderData, "No se asignó memoria para el backend.");
        return;
    }

    if (!neander.exitoso) {
        neanderDiagnostics.appendChild(createNeanderDiagnostic(neander.diagnosticos || []));
        showEmpty(neanderContent,
            "El programa fuente es válido, pero el backend NEANDER no pudo traducir toda la IR.");
        showEmpty(neanderData, "No se devuelve una generación parcial.");
        return;
    }

    const code = document.createElement("pre");
    code.className = "instruction-list neander-instructions";
    code.textContent = (neander.instrucciones || []).join("\n");
    neanderContent.appendChild(code);
    renderNeanderData(neander.datos || []);
}

function renderNeanderData(data) {
    if (data.length === 0) {
        showEmpty(neanderData, "No hay datos asignados.");
        return;
    }

    const wrapper = document.createElement("div");
    wrapper.className = "table-wrap";
    const table = document.createElement("table");
    table.innerHTML = "<thead><tr><th>Elemento</th><th>Clase</th><th>Dirección</th><th>Valor inicial</th></tr></thead>";
    const body = document.createElement("tbody");

    data.forEach((datum) => {
        const row = document.createElement("tr");
        [datum.nombre, formatOperandType(datum.tipo), datum.direccion,
            datum.valorInicial ?? "—"].forEach((value) => {
            const cell = document.createElement("td");
            cell.textContent = value;
            row.appendChild(cell);
        });
        body.appendChild(row);
    });

    table.appendChild(body);
    wrapper.appendChild(table);
    neanderData.appendChild(wrapper);
}

function formatOperandType(type) {
    return ({
        VARIABLE: "Variable",
        TEMPORAL: "Temporal IR",
        CONSTANTE: "Constante",
        TEMPORAL_INTERNO: "Temporal interno"
    })[type] || type;
}

function createNeanderDiagnostic(diagnostics) {
    const box = document.createElement("div");
    box.className = "warning-box";
    const heading = document.createElement("div");
    heading.className = "error-heading";
    const title = document.createElement("strong");
    title.textContent = "Limitación del backend NEANDER";
    const detail = document.createElement("button");
    detail.type = "button";
    detail.className = "error-detail-button";
    detail.textContent = "¿Por qué?";
    detail.addEventListener("click", () => showNeanderDiagnostic(diagnostics));
    heading.append(title, detail);
    box.appendChild(heading);

    const list = document.createElement("ul");
    diagnostics.forEach((diagnostic) => {
        const item = document.createElement("li");
        item.textContent = diagnostic;
        list.appendChild(item);
    });
    box.appendChild(list);
    return box;
}

function showNeanderDiagnostic(diagnostics) {
    const content = document.createDocumentFragment();
    content.appendChild(createParagraph(
        "El código fuente superó los análisis del compilador y produjo IR válida. La limitación pertenece al backend didáctico NEANDER, no al programa fuente."));
    const list = document.createElement("ul");
    diagnostics.forEach((diagnostic) => {
        const item = document.createElement("li");
        item.textContent = diagnostic;
        list.appendChild(item);
    });
    content.appendChild(list);
    openModal("Operación no soportada por NEANDER", content);
}

function renderErrors(container, errors = [], title, phase) {
    container.replaceChildren();
    if (errors.length > 0) {
        container.appendChild(createErrorBox(errors, title, phase));
    }
}

function createErrorBox(errors, title, phase) {
    const box = document.createElement("div");
    box.className = "error-box";
    const header = document.createElement("div");
    header.className = "error-heading";
    const heading = document.createElement("strong");
    heading.textContent = title;
    header.appendChild(heading);

    if (phase) {
        const detailButton = document.createElement("button");
        detailButton.type = "button";
        detailButton.className = "error-detail-button";
        detailButton.textContent = "Ver detalle";
        detailButton.addEventListener("click", () => showErrorDetail(phase, errors));
        header.appendChild(detailButton);
    }

    box.appendChild(header);

    const list = document.createElement("ul");
    errors.forEach((error) => {
        const item = document.createElement("li");
        item.textContent = error;
        list.appendChild(item);
    });
    box.appendChild(list);
    return box;
}

function showEmpty(container, message) {
    container.className = "empty-state";
    container.textContent = message;
}

function renderConnectionError(error) {
    setStatus("status-error", "✗ Error de conexión");
    resultMessage.textContent = error.message || "No fue posible conectar con el servidor.";
    renderErrors(lexicalErrors, [resultMessage.textContent], "Conexión", "Conexión");
    resetPipeline();
    showEmpty(tokensContent, "No se recibieron datos.");
    syntaxErrors.replaceChildren();
    showEmpty(astContent, "No se recibió un AST.");
    showEmpty(semanticErrors, "Análisis no disponible.");
    showEmpty(symbolsContent, "Tabla no disponible.");
    showEmpty(intermediateContent, "Código intermedio no disponible.");
    neanderDiagnostics.replaceChildren();
    showEmpty(neanderContent, "Código NEANDER no disponible.");
    showEmpty(neanderData, "Memoria NEANDER no disponible.");
}

function setStatus(className, text) {
    compileStatus.className = `status ${className}`;
    compileStatus.textContent = text;
}

function renderPipeline(result) {
    const lexicalErrorsList = result.erroresLexicos || [];
    const syntaxErrorsList = result.erroresSintacticos || [];
    const semanticErrorsList = result.erroresSemanticos || [];
    const sourceAvailable = Boolean(result.codigoFuente && result.codigoFuente.trim());

    setPhaseState("source", sourceAvailable ? "success" : "error");

    if (!sourceAvailable) {
        setPhaseState("lexical", "pending");
        setPhaseState("syntax", "pending");
        setPhaseState("semantic", "pending");
        setPhaseState("intermediate", "pending");
        setPhaseState("neander", "pending");
        return;
    }

    setPhaseState("lexical", lexicalErrorsList.length ? "error" : "success");
    if (lexicalErrorsList.length) {
        setPhaseState("syntax", "pending");
        setPhaseState("semantic", "pending");
        setPhaseState("intermediate", "pending");
        setPhaseState("neander", "pending");
        return;
    }

    setPhaseState("syntax", syntaxErrorsList.length ? "error" : "success");
    if (syntaxErrorsList.length) {
        setPhaseState("semantic", "pending");
        setPhaseState("intermediate", "pending");
        setPhaseState("neander", "pending");
        return;
    }

    setPhaseState("semantic", semanticErrorsList.length ? "error" : "success");
    setPhaseState("intermediate",
        semanticErrorsList.length || !result.exitoso ? "pending" : "success");
    if (semanticErrorsList.length || !result.neander?.ejecutado) {
        setPhaseState("neander", "pending");
    } else {
        setPhaseState("neander", result.neander.exitoso ? "success" : "warning");
    }
}

function resetPipeline() {
    ["source", "lexical", "syntax", "semantic", "intermediate", "neander"]
        .forEach((phase) => setPhaseState(phase, "pending"));
}

function setPhaseState(phase, state) {
    const step = document.querySelector(`[data-phase="${phase}"]`);
    const symbol = state === "success" ? "✓" : state === "error" ? "✗" : state === "warning" ? "!" : "○";
    step.className = `pipeline-step state-${state}`;
    step.querySelector("b").textContent = symbol;
}

function showTokenDetail(token) {
    const content = document.createDocumentFragment();
    content.appendChild(createDefinitionList([
        ["Tipo", token.tipo],
        ["Lexema / valor", token.valor],
        ["Línea", token.linea],
        ["Columna", token.columna]
    ]));
    content.appendChild(createParagraph(
        tokenExplanations[token.tipo] || "Elemento reconocido durante el análisis léxico."
    ));
    openModal("Detalle del token", content);
}

function showAstDetail(node) {
    const content = document.createDocumentFragment();
    const childrenCount = Array.isArray(node.hijos) ? node.hijos.length : 0;
    const details = [["Tipo", node.tipo], ["Cantidad de hijos", childrenCount]];
    if (node.valor !== null && node.valor !== undefined && node.valor !== "") {
        details.splice(1, 0, ["Valor", node.valor]);
    }
    content.appendChild(createDefinitionList(details));
    content.appendChild(createParagraph(
        astExplanations[node.tipo] || "Nodo producido por el analizador sintáctico."
    ));
    openModal("Detalle del nodo AST", content);
}

function showErrorDetail(phase, errors) {
    const content = document.createDocumentFragment();
    content.appendChild(createDefinitionList([["Fase", phase]]));
    const list = document.createElement("ul");
    errors.forEach((error) => {
        const item = document.createElement("li");
        item.textContent = error;
        list.appendChild(item);
    });
    content.appendChild(list);
    content.appendChild(createParagraph(
        "Las fases posteriores pueden no haberse ejecutado debido a estos errores."
    ));
    openModal("Detalle de errores", content);
}

function createDefinitionList(entries) {
    const list = document.createElement("dl");
    entries.forEach(([term, description]) => {
        const name = document.createElement("dt");
        name.textContent = term;
        const value = document.createElement("dd");
        value.textContent = description;
        list.append(name, value);
    });
    return list;
}

function createParagraph(text) {
    const paragraph = document.createElement("p");
    paragraph.textContent = text;
    return paragraph;
}

function openModal(title, content) {
    previousFocus = document.activeElement;
    modalTitle.textContent = title;
    modalContent.replaceChildren(content);
    modal.hidden = false;
    document.body.classList.add("modal-open");
    modalDialog.focus();
}

function closeModal() {
    if (modal.hidden) return;
    modal.hidden = true;
    document.body.classList.remove("modal-open");
    previousFocus?.focus();
}
