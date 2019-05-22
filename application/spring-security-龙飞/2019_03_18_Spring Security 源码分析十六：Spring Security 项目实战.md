title: 【龙飞】Spring Security 源码分析十六：Spring Security 项目实战
date: 2019-03-18
tag:
categories: Spring Security
permalink: Spring-Security/longfei/The-project-of-actual-combat
author: 龙飞
from_url: http://niocoder.com/2018/03/18/Spring-Security源码分析十六-Spring-Security项目实战/

-------

摘要: 原创出处 http://niocoder.com/2018/03/18/Spring-Security源码分析十六-Spring-Security项目实战/ 「龙飞」欢迎转载，保留摘要，谢谢！

- [1. 前言](http://www.iocoder.cn/Spring-Security/longfei/The-project-of-actual-combat/)
  - [1.1 技术栈](http://www.iocoder.cn/Spring-Security/longfei/The-project-of-actual-combat/)
  - [1.2 效果图](http://www.iocoder.cn/Spring-Security/longfei/The-project-of-actual-combat/)
  - [1.3 部分代码](http://www.iocoder.cn/Spring-Security/longfei/The-project-of-actual-combat/)
  - [1.4 启动方式](http://www.iocoder.cn/Spring-Security/longfei/The-project-of-actual-combat/)
- [2. 代码下载](http://www.iocoder.cn/Spring-Security/longfei/The-project-of-actual-combat/)
- [3. 推荐文章](http://www.iocoder.cn/Spring-Security/longfei/The-project-of-actual-combat/)

-------

> Spring Security是一个能够为基于Spring的企业应用系统提供声明式的安全访问控制解决方案的安全框架。它提供了一组可以在Spring应用上下文中配置的Bean，充分利用了Spring IoC，DI（控制反转Inversion of Control ,DI:Dependency Injection 依赖注入）和AOP（面向切面编程）功能，为应用系统提供声明式的安全访问控制功能，减少了为企业系统安全控制编写大量重复代码的工作。


# 1. 前言

本章是根据前面[Spring Security系列](https://longfeizheng.github.io/categories/#Security)实现一个基于角色的权限管理系统。

## 1.1 技术栈

- Spring Boot
- Spring Security
- Spring Social（需配置host`127.0.0.1 www.merryyou.cn`）
- Spring Data JPA
- Freemarker
- Mysql
- Redis
- 前端miniui(**非开源**)

## 1.2 效果图

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_01.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_01.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_01.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_01.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_01.png")
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_02.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_02.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_02.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_02.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_02.png")
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_03.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_03.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_03.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_03.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_03.png")
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_04.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_04.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_04.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_04.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_04.png")
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_05.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_05.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_05.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_05.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_05.png")
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_06.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_06.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_06.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_06.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_06.png")
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_07.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_07.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_07.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_07.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_07.png")
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_08.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_08.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_08.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_08.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_08.png")
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_09.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_09.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_09.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_09.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_09.png")
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_10.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_10.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_10.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_10.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/logback/logback_10.png")

## 1.3 部分代码

```javascript
$.ajax({
            url: "${re.contextPath}/connect",
            type: "get",
            async: true,
            dataType: "json",
            success: function (data) {
                if (data.code === 0) {
                    if (data.data.qq) {
                        //解绑
                        $("#bindingQq").attr("title", "解绑")
                        $(".fa-qq").addClass("social_title");
                    } else {
                        //绑定
                        $("#bindingQq").attr("title", "绑定")
                        $(".fa-qq").removeClass("social_title");
                    }
                    if (data.data.weixin) {
                        //解绑
                        $("#bindingWeixin").attr("title", "解绑")
                        $(".fa-weixin").addClass("social_title");
                    } else {
                        //绑定
                        $("#bindingWeixin").attr("title", "绑定")
                        $(".fa-weixin").removeClass("social_title");
                    }
                    if (data.data.weibo) {
                        //解绑
                        $("#bindingWeibo").attr("title", "解绑")
                        $(".fa-weibo").addClass("social_title");
                    } else {
                        //绑定
                        $("#bindingWeibo").attr("title", "绑定")
                        $(".fa-weibo").removeClass("social_title");
                    }
                }
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                alert(XMLHttpRequest.status);
                alert(XMLHttpRequest.readyState);
                alert(textStatus); // paser error;
            }

        });
```
```javascript
$.ajax({
                url: "${re.contextPath}/role/" + data.id,
                cache: false,
                success: function (text) {
                    var o = mini.decode(text);
                    //设置数的选中状态
                    console.log(o.menuIds);
                    var nodes = tree.getAllChildNodes(tree.getRootNode());
                    for(var i=0;i<nodes.length;i++){
                        if(o.menuIds.indexOf(nodes[i]['id'])>=0){
                            tree.checkNode(nodes[i]);
                        }else{
                            tree.uncheckNode(nodes[i]);
                        }
                    }
                    form.setData(o);
                    form.setChanged(false);
                }
            });
```

```java
    @Override
    public List<MenuDto> getMenusList() {
        return repository.findAll().stream()
                .map(e ->new MenuDto(e.getId(), e.getPId(), e.getName(), e.getUrl()))
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> getUrlByname(String username) {
        Set<SysMenu> mesnus = new HashSet<>();
        userRepository.findByUsername(username)
                .getRoles()
                .forEach(e->mesnus.addAll(e.getMenus()));
        return mesnus.stream().map(e->e.getUrl()).collect(Collectors.toSet());
    }
```

```java
protected void configure(HttpSecurity http) throws Exception {
//        http.addFilterBefore(validateCodeFilter, UsernamePasswordAuthenticationFilter.class)
        http.headers().frameOptions().disable().and()
                .formLogin()//使用表单登录，不再使用默认httpBasic方式
                .loginPage(SecurityConstants.DEFAULT_UNAUTHENTICATION_URL)//如果请求的URL需要认证则跳转的URL
                .loginProcessingUrl(SecurityConstants.DEFAULT_SIGN_IN_PROCESSING_URL_FORM)//处理表单中自定义的登录URL
                .successHandler(merryyouLoginSuccessHandler)//登录成功处理器，返回JSON
                .failureHandler(merryyouAuthenticationfailureHandler)//登录失败处理器
                .and()
                .apply(validateCodeSecurityConfig)//验证码拦截
                .and()
                .apply(smsCodeAuthenticationSecurityConfig)
                .and()
                .apply(merryyouSpringSocialConfigurer)//社交登录
                .and()
                .rememberMe()
                .tokenRepository(persistentTokenRepository())
                .tokenValiditySeconds(securityProperties.getRememberMeSeconds())
                .userDetailsService(userDetailsService)
                .and()
                .sessionManagement()
//                .invalidSessionStrategy(invalidSessionStrategy)
                .invalidSessionUrl("/session/invalid")
                .maximumSessions(securityProperties.getSession().getMaximumSessions())//最大session并发数量1
                .maxSessionsPreventsLogin(securityProperties.getSession().isMaxSessionsPreventsLogin())//之后的登录踢掉之前的登录
                .expiredSessionStrategy(sessionInformationExpiredStrategy)
                .and()
                .and()
                .logout()
                .logoutUrl("/signOut")//默认退出地址/logout
                .logoutSuccessUrl("/")//退出之后跳转到注册页面
                .deleteCookies("JSESSIONID")
                .and()
                .authorizeRequests().antMatchers(SecurityConstants.DEFAULT_UNAUTHENTICATION_URL,
                SecurityConstants.DEFAULT_SIGN_IN_PROCESSING_URL_FORM,
                SecurityConstants.DEFAULT_REGISTER_URL,
                SecurityConstants.DEFAULT_SIGN_IN_PROCESSING_URL_MOBILE,
                SecurityConstants.DEFAULT_SIGN_IN_URL_MOBILE_PAGE,
                "/register",
                "/socialRegister",//社交账号注册和绑定页面
                "/user/register",//处理社交注册请求
                "/social/info",//获取当前社交用户信息
                "/session/invalid",
                "/**/*.js",
                "/**/*.css",
                "/**/*.jpg",
                "/**/*.png",
                "/**/*.woff2",
                "/code/*")
                .permitAll()//以上的请求都不需要认证
                //.antMatchers("/").access("hasRole('USER')")
                .and()
                .csrf().disable()//关闭csrd拦截
        ;
        //安全模块单独配置
        authorizeConfigProvider.config(http.authorizeRequests());
    }
```

```java
@PreAuthorize("hasAnyAuthority('user:select','user:update')")
    @PostMapping(value = "/user/saveUser")
    @ResponseBody
    public Result saveUser(@RequestParam String data) {
        log.info(data);
        return sysUserService.save(data);
    }
```
```html
<td style="width:100%;">
                     <@sec.authorize access="hasAuthority('role:add')">
                    <a class="mini-button" iconCls="icon-add" onclick="add()">增加</a>
                     </@sec.authorize>
                     <@sec.authorize access="hasAuthority('role:update')">
                    <a class="mini-button" iconCls="icon-add" onclick="edit()">编辑</a>
                     <@sec.authorize access="hasAuthority('role:del')">
                      </@sec.authorize>
                    <a class="mini-button" iconCls="icon-remove" onclick="remove()">删除</a>
                     </@sec.authorize>
                </td>
```



## 1.4 启动方式
1. idea 配置lombok插件,参考[lombok-intellij-plugin](https://github.com/mplushnikov/lombok-intellij-plugin/)
2. 修改application.yml中数据源信息，执行db文件夹下面的sql文件
3. 修改application-dev.yml 中redis连接信息
4. 社交登录需配置host文件：`127.0.0.1 www.merryyou.cn` 微信`appid`已过期

# 2. 代码下载

- github：[https://github.com/longfeizheng/logback](https://github.com/longfeizheng/logback)
- gitee：[https://gitee.com/merryyou/logback](https://gitee.com/merryyou/logback)

# 3. 推荐文章
1. [【译】用Java创建你的第一个区块链-part1](https://longfeizheng.github.io/2018/03/10/%E7%94%A8Java%E5%88%9B%E5%BB%BA%E4%BD%A0%E7%9A%84%E7%AC%AC%E4%B8%80%E4%B8%AA%E5%8C%BA%E5%9D%97%E9%93%BE-part1/)
2. [【译】用Java创建你的第一个区块链-part2:可交易](https://longfeizheng.github.io/2018/03/11/%E7%94%A8Java%E5%88%9B%E5%BB%BA%E4%BD%A0%E7%9A%84%E7%AC%AC%E4%B8%80%E4%B8%AA%E5%8C%BA%E5%9D%97%E9%93%BE-part2/)