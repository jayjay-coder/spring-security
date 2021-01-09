## 定制 Spring Security 中的表单登录

## 1.**登录接口**

在 Spring Security 中，如果我们不做任何配置，默认的登录页面和登录接口的地址都是 `/login`，也就是说，默认会存在如下两个请求：

- GET http://localhost:8080/login
- POST http://localhost:8080/login

如果是 GET 请求表示你想访问登录页面，如果是 POST 请求，表示你想提交登录数据。

我们在 SecurityConfig 中自定定义了登录页面地址，如下：

```java
.and()
.formLogin()
.loginPage("/login.html")
.permitAll()
.and()
```

当我们配置了 loginPage 为 `/login.html` 之后，这个配置从字面上理解，就是设置登录页面的地址为 `/login.html`。

实际上它还有一个隐藏的操作，就是登录接口地址也设置成 `/login.html` 了。换句话说，新的登录页面和登录接口地址都是 `/login.html`，现在存在如下两个请求：

- GET http://localhost:8080/login.html
- POST http://localhost:8080/login.html

前面的 GET 请求用来获取登录页面，后面的 POST 请求用来提交登录数据。

有的小伙伴会感到奇怪？为什么登录页面和登录接口不能分开配置呢？

其实是可以分开配置的！

在 SecurityConfig 中，我们可以通过 loginProcessingUrl 方法来指定登录接口地址，如下：

```java.and()
.formLogin()
.loginPage("/login.html")
.loginProcessingUrl("/doLogin")
.permitAll()
.and()
```

这样配置之后，登录页面地址和登录接口地址就分开了，各是各的。

此时我们还需要修改登录页面里边的 action 属性，改为 `/doLogin`，如下：

```html<form action="/doLogin" method="post">
<form action="/doLogin" method="post">
<!--省略-->
</form>
```

此时，启动项目重新进行登录，我们发现依然可以登录成功。

那么为什么默认情况下两个配置地址是一样的呢？

我们知道，form 表单的相关配置在 FormLoginConfigurer 中，该类继承自 AbstractAuthenticationFilterConfigurer ，所以当 FormLoginConfigurer 初始化的时候，AbstractAuthenticationFilterConfigurer 也会初始化，在 AbstractAuthenticationFilterConfigurer 的构造方法中，我们可以看到：

```javaprotected AbstractAuthenticationFilterConfigurer() {
protected AbstractAuthenticationFilterConfigurer() {
	setLoginPage("/login");
}
```

这就是配置默认的 loginPage 为 `/login`。

另一方面，FormLoginConfigurer 的初始化方法 init 方法中也调用了父类的 init 方法：

```javapublic void init(H http) throws Exception {
public void init(H http) throws Exception {
	super.init(http);
	initDefaultLoginFilter(http);
}
```

而在父类的 init 方法中，又调用了 updateAuthenticationDefaults，我们来看下这个方法:

```javaprotected final void updateAuthenticationDefaults() {
protected final void updateAuthenticationDefaults() {
	if (loginProcessingUrl == null) {
		loginProcessingUrl(loginPage);
	}
	//省略
}
```

从这个方法的逻辑中我们就可以看到，如果用户没有给 loginProcessingUrl 设置值的话，默认就使用 loginPage 作为 loginProcessingUrl。

而如果用户配置了 loginPage，在配置完 loginPage 之后，updateAuthenticationDefaults 方法还是会被调用，此时如果没有配置 loginProcessingUrl，则使用新配置的 loginPage 作为 loginProcessingUrl。

好了，看到这里，相信小伙伴就明白了为什么一开始的登录接口和登录页面地址一样了。

## **2.登录参数**

说完登录接口，我们再来说登录参数。

我们的登录表单中的参数是 username 和 password，注意，默认情况下，这个不能变：

```html
<form action="/login.html" method="post">
    <input type="text" name="username" id="name">
    <input type="password" name="password" id="pass">
    <button type="submit">
      <span>登录</span>
    </button>
</form>
```

那么为什么是这样呢？

还是回到 FormLoginConfigurer 类中，在它的构造方法中，我们可以看到有两个配置用户名密码的方法：

```java
public FormLoginConfigurer() {
	super(new UsernamePasswordAuthenticationFilter(), null);
	usernameParameter("username");
	passwordParameter("password");
}
```

在这里，首先 super 调用了父类的构造方法，传入了 UsernamePasswordAuthenticationFilter 实例，该实例将被赋值给父类的 authFilter 属性。

接下来 usernameParameter 方法如下：

```java
public FormLoginConfigurer<H> usernameParameter(String usernameParameter) {
	getAuthenticationFilter().setUsernameParameter(usernameParameter);
	returnthis;
}
```

getAuthenticationFilter 实际上是父类的方法，在这个方法中返回了 authFilter 属性，也就是一开始设置的 UsernamePasswordAuthenticationFilter 实例，然后调用该实例的 setUsernameParameter 方法去设置登录用户名的参数：

```java
public void setUsernameParameter(String usernameParameter) {
	this.usernameParameter = usernameParameter;
}
```

getAuthenticationFilter 实际上是父类的方法，在这个方法中返回了 authFilter 属性，也就是一开始设置的 UsernamePasswordAuthenticationFilter 实例，然后调用该实例的 setUsernameParameter 方法去设置登录用户名的参数：

```java
public void setUsernameParameter(String usernameParameter) {
	this.usernameParameter = usernameParameter;
}
```

这里的设置有什么用呢？当登录请求从浏览器来到服务端之后，我们要从请求的 HttpServletRequest 中取出来用户的登录用户名和登录密码，怎么取呢？还是在 UsernamePasswordAuthenticationFilter 类中，有如下两个方法：

```java
protected String obtainPassword(HttpServletRequest request) {
	return request.getParameter(passwordParameter);
}
protected String obtainUsername(HttpServletRequest request) {
	return request.getParameter(usernameParameter);
}
```

可以看到，这个时候，就用到默认配置的 username 和 password 了。

当然，这两个参数我们也可以自己配置，自己配置方式如下：

```java
.and()
.formLogin()
.loginPage("/login.html")
.loginProcessingUrl("/doLogin")
.usernameParameter("name")
.passwordParameter("passwd")
.permitAll()
.and()
```

配置完成后，也要修改一下前端页面：

```html
<form action="/doLogin" method="post">
    <div class="input">
        <label for="name">用户名</label>
        <input type="text" name="name" id="name">
        <span class="spin"></span>
    </div>
    <div class="input">
        <label for="pass">密码</label>
        <input type="password" name="passwd" id="pass">
        <span class="spin"></span>
    </div>
    <div class="button login">
        <button type="submit">
            <span>登录</span>
            <i class="fa fa-check"></i>
        </button>
    </div>
</form>
```

注意修改 input 的 name 属性值和服务端的对应。

配置完成后，重启进行登录测试。

## **3.登录回调**

### 3.1 登录成功回调

在 Spring Security 中，和登录成功重定向 URL 相关的方法有两个：

- defaultSuccessUrl
- successForwardUrl

这两个咋看没什么区别，实际上内藏乾坤。

首先我们在配置的时候，defaultSuccessUrl 和 successForwardUrl 只需要配置一个即可，具体配置哪个，则要看你的需求，两个的区别如下：

1. defaultSuccessUrl 有一个重载的方法，我们先说一个参数的 defaultSuccessUrl 方法。如果我们在 defaultSuccessUrl 中指定登录成功的跳转页面为 `/index`，此时分两种情况，如果你是直接在浏览器中输入的登录地址，登录成功后，就直接跳转到 `/index`，如果你是在浏览器中输入了其他地址，例如 `http://localhost:8080/hello`，结果因为没有登录，又重定向到登录页面，此时登录成功后，就不会来到 `/index` ，而是来到 `/hello` 页面。
2. defaultSuccessUrl 还有一个重载的方法，第二个参数如果不设置默认为 false，也就是我们上面的的情况，如果手动设置第二个参数为 true，则 defaultSuccessUrl 的效果和 successForwardUrl 一致。
3. successForwardUrl 表示不管你是从哪里来的，登录后一律跳转到 successForwardUrl 指定的地址。例如 successForwardUrl 指定的地址为 `/index` ，你在浏览器地址栏输入 `http://localhost:8080/hello`，结果因为没有登录，重定向到登录页面，当你登录成功之后，就会服务端跳转到 `/index` 页面；或者你直接就在浏览器输入了登录页面地址，登录成功后也是来到 `/index`。

相关配置如下：

```java
.and()
.formLogin()
.loginPage("/login.html")
.loginProcessingUrl("/doLogin")
.usernameParameter("name")
.passwordParameter("passwd")
.defaultSuccessUrl("/index")
.successForwardUrl("/index")
.permitAll()
.and()
```

**「注意：实际操作中，defaultSuccessUrl 和 successForwardUrl 只需要配置一个即可。」**

### 3.2 登录失败回调

与登录成功相似，登录失败也是有两个方法：

- failureForwardUrl
- failureUrl

**「这两个方法在设置的时候也是设置一个即可」**。failureForwardUrl 是登录失败之后会发生服务端跳转，failureUrl 则在登录失败之后，会发生重定向。

## **4.注销登录**

注销登录的默认接口是 `/logout`，我们也可以配置。

```java
.and()
.logout()
.logoutUrl("/logout")
.logoutRequestMatcher(new AntPathRequestMatcher("/logout","POST"))
.logoutSuccessUrl("/index")
.deleteCookies()
.clearAuthentication(true)
.invalidateHttpSession(true)
.permitAll()
.and()
```

注销登录的配置我来说一下：

1. 默认注销的 URL 是 `/logout`，是一个 GET 请求，我们可以通过 logoutUrl 方法来修改默认的注销 URL。
2. logoutRequestMatcher 方法不仅可以修改注销 URL，还可以修改请求方式，实际项目中，这个方法和 logoutUrl 任意设置一个即可。
3. logoutSuccessUrl 表示注销成功后要跳转的页面。
4. deleteCookies 用来清除 cookie。
5. clearAuthentication 和 invalidateHttpSession 分别表示清除认证信息和使 HttpSession 失效，默认可以不用配置，默认就会清除