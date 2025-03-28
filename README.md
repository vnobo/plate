<h1 style="text-align: center;align-content: center">
Platform For Plate
</h1>

<div style="text-align: center;align-content: center">

The system management platform is an application developed based on Spring Boot 3 and Angular. It is used to manage
basic system information such as users, roles, tenants, menus, etc., and provides basic platform functions.

</div>

## ✨ Features

- Provide unified user, role, and tenant management.
- Provide permission control function.
- Provide menu management functions.
- Provide basic platform functions, such as log management, audit management, etc..

## ☀️ License

[MIT](./LICENSE)

## 🖥 Environment Support

* Spring boot `3.4.3`
* SSL support

```shell
keytool -genkeypair -alias plate -keyalg RSA -keysize 2048 -validity 365 -keystore plate.jks -storetype JKS -storepass 123456 -keypass 123456 -dname "CN=localhost, OU=PlateUnit, O=PlateOrg, L=Xi'an, ST=Shaanxi, C=CN"
```

```shell
  keytool -importkeystore -srckeystore plate.jks -srcstoretype JKS -srcstorepass 123456 -destkeystore plate.p12 -deststoretype PKCS12 -deststorepass 123456
```

Angular `^19.0.0`

* ng-zorro-antd `17.2.0`
* Server-side Rendering
* Modern browsers including the following [specific versions](https://angular.io/guide/browser-support)
* [Electron](http://electron.atom.io/)

## 📦 Installation

**We recommend using `@angular/cli` to install**. It not only makes development easier, but also allows you to take
advantage of the rich ecosystem of angular packages and tooling.

```bash
$ ng new PROJECT_NAME
$ cd PROJECT_NAME
$ ng add plate-platform
```

> More information about `@angular/cli` [here](https://github.com/angular/angular-cli).

You can also install `plate-platform` with npm or yarn

```bash
$ npm install plate-platform
```

## 🔨 Usage

Import the component modules you want to use into your `app.module.ts` file
and [feature modules](https://angular.io/guide/feature-modules).

```ts
import {NzButtonModule} from 'plate-platform/button';

@NgModule({
    imports: [NzButtonModule]
})
export class AppModule {
}
```

> `@angular/cli` users won't have to worry about the things below, but it's good to know.

And import style and SVG icon assets file link in `angular.json`.

```diff
{
  "assets": [
+   {
+     "glob": "**/*",
+     "input": "./node_modules/@ant-design/icons-angular/src/inline-svg/",
+     "output": "/assets/"
+   }
  ],
  "styles": [
+   "node_modules/ng-zorro-antd/plate-platform.min.css"
  ]
}
```

See [Getting Started](https://ng.ant.design/docs/getting-started/en) for more details.

## 🔗 Links

* [ng-zorro-antd](https://github.com/NG-ZORRO/ng-zorro-antd-mobile)
* [ng-alain](https://github.com/ng-alain/ng-alain)
* [Snippet extension for VSCode](https://marketplace.visualstudio.com/items?itemName=cipchk.ng-zorro-vscode)

## ⌨️ Development

```bash
$ git clone https://github.com/vnobo/plate.git
$ cd plate/ui/
$ npm install
$ npm run start
```

Browser would open automatically.

## 🤝 Contributing

[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](https://github.com/NG-ZORRO/ng-zorro-antd/pulls)

We welcome all contributions.You can submit any
ideas as [pull requests](https://github.com/NG-ZORRO/ng-zorro-antd/pulls) or
as [GitHub issues](https://github.com/NG-ZORRO/ng-zorro-antd/issues).


> If you're new to posting issues, we ask that you read [*How To Ask Questions The Smart
Way*](http://www.catb.org/~esr/faqs/smart-questions.html) (**This guide does not provide actual support services for
this project!**), [How to Ask a Question in Open Source Community](https://github.com/seajs/seajs/issues/545)
> and [How to Report Bugs Effectively](http://www.chiark.greenend.org.uk/~sgtatham/bugs.html) prior to posting. Well
> written bug reports help us help you!

Thanks to [JetBrains](https://www.jetbrains.com/?from=ng-zorro-antd) for supporting us free open source licenses.

[![JetBrains](https://img.alicdn.com/tfs/TB1sSomo.z1gK0jSZLeXXb9kVXa-120-130.svg)](https://www.jetbrains.com/?from=ng-zorro-antd)

## ❓ Help from the Community

For questions on how to use plate-platform, please post questions
to [<img alt="Stack Overflow" src="https://cdn.sstatic.net/Sites/stackoverflow/company/img/logos/so/so-logo.svg?v=2bb144720a66" width="140" />](http://stackoverflow.com/questions/tagged/ng-zorro-antd)
using the `plate-platform` tag. If you're not finding what you need on stackoverflow, you can find us
on [![Discord](https://img.shields.io/discord/748677963142135818?label=Discord&style=flat-square)](https://discord.com/channels/748677963142135818/764322550712893451)
as well.

As always, we encourage experienced users to help those who are not familiar with `plate-platform`!

## 🎉 Users

> We list some users here!