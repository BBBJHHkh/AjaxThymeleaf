# Dashboard iframe 多子页面功能说明

## 📌 功能概览

新增一个**综合管理后台**模块，主页面通过两个 `<iframe>` 嵌入两个独立的子页面，每个子页面是独立的 HTTP 请求，可以单独刷新、单独传参，**互不影响**。

> 重点演示：iframe 局部刷新 + 子页面独立路由 + 父子页面之间通过 URL 参数交互。

| 子模块 | URL | 功能 |
|---|---|---|
| 主页面 | `/dashboard` | 包含两个 iframe 的容器 |
| 子页面 1 | `/dashboard/stats` | 实时监控卡片（静态数据） |
| 子页面 2 | `/dashboard/report` | 行业动态报表（带下拉联动） |

---

## 📁 文件清单

### 新增文件

| 路径 | 作用 |
|---|---|
| `src/main/java/net/hka/examples/thymeleaf/web/controller/DashboardController.java` | 一个 Controller 处理 3 个路由 |
| `src/main/resources/templates/dashboard/layout.html` | 主页面：嵌入两个 iframe |
| `src/main/resources/templates/dashboard/stats.html` | 子页面 1：实时监控 |
| `src/main/resources/templates/dashboard/report.html` | 子页面 2：动态报表 + 下拉联动 |

### 修改文件

| 路径 | 改动 |
|---|---|
| `src/main/resources/templates/fragments/header.html` | 在导航栏增加「Dashboard (iframe Demo)」入口，并用 `module == 'dashboard'` 高亮当前页 |

> 临时目录 `spring_boot_iframe_demo/`（最早搭的原型）已删除，全部代码已并入主项目。

---

## 🔀 路由设计

`DashboardController` 一个 Controller 装 3 个路由（控制器签名见末尾附录）：

```
GET /dashboard            →  templates/dashboard/layout.html
GET /dashboard/stats      →  templates/dashboard/stats.html
GET /dashboard/report     →  templates/dashboard/report.html
                             （接受 ?category=tech|finance|lifestyle，默认 tech）
```

通过 `@ModelAttribute("module")` 统一注入当前模块名 `dashboard`，让顶部导航条能根据 `${module == 'dashboard'}` 自动高亮。

---

## 🧩 核心实现原理

### 1. 主页面通过 Thymeleaf `@{/...}` 渲染 iframe URL

```html:11:15:templates/dashboard/layout.html
    <!-- 第一个子页面：系统实时状态 -->
    <div class="module-box">
        <h3>模块一：系统实时状态</h3>
        <iframe th:src="@{/dashboard/stats}"></iframe>
    </div>

    <!-- 第二个子页面：动态数据报表（带下拉联动） -->
    <div class="module-box">
        <h3>模块二：行业动态报表（带下拉联动）</h3>
        <iframe th:src="@{/dashboard/report}"></iframe>
    </div>
```

**关键点**：使用 Thymeleaf 的 `@{/dashboard/stats}` 表达式，渲染时会**自动拼接 context path**。

由于 `application.properties` 中设置了 `server.servlet.context-path=/thymeleaf`，浏览器最终拿到的 iframe 地址是：

```
http://localhost:8080/thymeleaf/dashboard/stats
http://localhost:8080/thymeleaf/dashboard/report
```

> ⚠️ 千万不能写 `<iframe src="/dashboard/stats">`，那样浏览器会请求 `http://localhost:8080/dashboard/stats`，缺 context path，会 404。

### 2. Controller 返回三个不同的视图名

```java:21:37:DashboardController.java
    /** 1. Main page: hosts two iframes pointing at the sub-pages. */
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard/layout";
    }

    /** 2. Sub-page one: real-time system stats (static for the demo). */
    @GetMapping("/dashboard/stats")
    public String statsPage(Model model) {
        model.addAttribute("activeUsers", 1314);
        model.addAttribute("systemStatus", "运行正常 (Healthy)");
        return "dashboard/stats";
    }
```

### 3. 子页面 2 的下拉联动：JS 内联拿 base URL

`<select>` 切换分类时，需要让 iframe 内部跳转到 `/thymeleaf/dashboard/report?category=xxx`。**直接在 JS 里写 `/dashboard/report` 会 404**（缺 context path），所以用 Thymeleaf 的 JS 内联语法把 base URL 渲染出来：

```html:54:64:templates/dashboard/report.html
    <script th:inline="javascript">
        /*<![CDATA[*/
        // Thymeleaf inline: context path is added automatically, so this works
        // even if server.servlet.context-path in application.properties changes.
        var reportBaseUrl = /*[[@{/dashboard/report}]]*/ '/dashboard/report';
        function changeCategory(selectElement) {
            window.location.href = reportBaseUrl + '?category=' + selectElement.value;
        }
        /*]]>*/
    </script>
```

Thymeleaf 渲染后的 HTML：

```javascript
var reportBaseUrl = "\/thymeleaf\/dashboard\/report";
function changeCategory(selectElement) {
    window.location.href = reportBaseUrl + '?category=' + selectElement.value;
}
```

> 即使将来 `application.properties` 改了 context path，模板也无需任何改动。

### 4. 下拉选项的选中状态靠后端 echo

```html:30:36:templates/dashboard/report.html
    <div class="control-bar">
        <label for="categorySelect"><b>请选择行业分类：</b></label>
        <select id="categorySelect" onchange="changeCategory(this)">
            <option value="tech"      th:selected="${currentCategory == 'tech'}">科技板块</option>
            <option value="finance"   th:selected="${currentCategory == 'finance'}">金融板块</option>
            <option value="lifestyle" th:selected="${currentCategory == 'lifestyle'}">消费板块</option>
        </select>
    </div>
```

Controller 把请求参数 `category` 回填到 `currentCategory` 模型属性：

```java:40:55:DashboardController.java
    @GetMapping("/dashboard/report")
    public String reportPage(
            @RequestParam(name = "category", defaultValue = "tech") String category,
            Model model) {

        model.addAttribute("currentCategory", category);

        if ("tech".equals(category)) {
            model.addAttribute("reportTitle", "🚀 科技板块报表");
            model.addAttribute("reportData", "AI大模型、芯片算力、云服务市场需求持续暴涨。");
        } else if ("finance".equals(category)) {
            model.addAttribute("reportTitle", "💰 金融板块报表");
            model.addAttribute("reportData", "银行信贷规模稳定，证券市场迎来新一轮政策利好。");
        } else if ("lifestyle".equals(category)) {
            model.addAttribute("reportTitle", "🛍️ 消费板块报表");
            model.addAttribute("reportData", "线下零售与文旅市场复苏，客流量创近期新高。");
        }

        return "dashboard/report";
    }
```

这样切换分类后刷新页面，下拉框仍然停留在刚选的那一项（不会跳回默认的「科技板块」）。

---

## 🎬 操作演示

### 步骤 1：登录（如果尚未登录）

```
http://localhost:8080/thymeleaf/signin
```

### 步骤 2：进入 Dashboard

点击顶部导航栏新加的 **「Dashboard (iframe Demo)」**，或在地址栏访问：

```
http://localhost:8080/thymeleaf/dashboard
```

页面会同时加载两个 iframe：

- **模块一**：实时监控卡片（活跃用户数、系统状态）
- **模块二**：行业动态报表（下拉框 + 报表内容）

### 步骤 3：测试下拉联动

在模块二的下拉框里选择「金融板块」或「消费板块」，iframe 内部会发起独立请求：

```
GET /thymeleaf/dashboard/report?category=finance
GET /thymeleaf/dashboard/report?category=lifestyle
```

刷新子页面 + 切换选中项 + 更新报表文案，**主页面完全不动**。

---

## 🐞 踩过的坑

### ❌ 404 Not Found — `/dashboard/report?category=finance`

**症状**：浏览器 DevTools Network 面板显示请求 URL 是：

```
http://localhost:8080/dashboard/report?category=finance
```

而不是正确的：

```
http://localhost:8080/thymeleaf/dashboard/report?category=finance
```

**原因**：`report.html` 里一开始的 JS 写的是：

```javascript
window.location.href = '/dashboard/report?category=' + selectedValue;  // ❌ 硬编码，缺 context path
```

`server.servlet.context-path=/thymeleaf` 是 Tomcat 强制加的，JS 里硬编码的路径如果不带 `/thymeleaf` 前缀，就直接 404。

**修复**：改用 Thymeleaf JS 内联，让 base URL 由模板渲染：

```javascript
var reportBaseUrl = /*[[@{/dashboard/report}]]*/ '/dashboard/report';
window.location.href = reportBaseUrl + '?category=' + selectElement.value;  // ✅
```

**经验**：

1. 凡是 JS / `<a href>` / `<form action>` / `<iframe src>` 里的 URL，**不要硬编码 `/xxx`** —— 一律用 Thymeleaf `@{/xxx}`。
2. 即使将来 context path 改了，模板无需任何改动。

---

## 🚀 后续扩展建议

### 1. 把硬编码数据换成数据库查询

`/dashboard/stats` 当前 `activeUsers` 和 `systemStatus` 是写死的 1314 和「运行正常」。可以接 `Runtime`/`OperatingSystemMXBean` 拿真实指标：

```java
@GetMapping("/dashboard/stats")
public String statsPage(Model model) {
    Runtime runtime = Runtime.getRuntime();
    long totalMemory = runtime.totalMemory() / (1024 * 1024);
    long freeMemory  = runtime.freeMemory()  / (1024 * 1024);

    model.addAttribute("activeUsers", userService.countActiveSessions());
    model.addAttribute("systemStatus", "运行正常 (Healthy)");
    model.addAttribute("totalMemoryMB", totalMemory);
    model.addAttribute("freeMemoryMB",  freeMemory);
    return "dashboard/stats";
}
```

### 2. 报表数据接真实数据源

`/dashboard/report` 现在是写死的 3 段话。可以改成：

- 查数据库（按行业分类聚合订单/客户数据）
- 调外部 API（行情、新闻）
- 接缓存（Redis 定时刷新数据，Controller 走缓存查）

### 3. 用 AJAX 替代 iframe 跳转

iframe 跳转会让子页面整体闪一下，体验一般。更现代的做法是 AJAX 局部刷新：

```javascript
function changeCategory(selectElement) {
    var category = selectElement.value;
    $.get(/*[[@{/dashboard/report}]]*/, { category: category })
        .done(function (html) {
            $('#reportContainer').html(html);  // 局部替换
        });
}
```

但这就脱离了「iframe 隔离」的初衷，根据业务选型。

---

## 📝 总结

| 维度 | 实现要点 |
|---|---|
| **父子隔离** | 主页面 + 两个 iframe = 三次独立 HTTP 请求 |
| **联动交互** | 子页面内部 `window.location.href` 触发自己的局部刷新 |
| **Context path 适配** | HTML 用 `@{/...}`，JS 用 `/*[[@{/...}]]*/`，**没有任何路径硬编码** |
| **选中状态保持** | 后端 `@RequestParam` 回填 → `th:selected` 高亮当前 option |
| **导航高亮** | `@ModelAttribute("module")` + `th:classappend="${module == 'dashboard' ? 'active' : ''}"` |

🎉 **完成！Dashboard iframe Demo 已并入主项目，可从顶部导航栏进入体验。**
