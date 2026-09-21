package com.example.xrstudy.ui.navigation

import kotlinx.serialization.Serializable

/**
 * 画面の「宛先（ルート）」。
 *
 * 宛先を、文字列（"home" など）ではなく、**型**で表す書き方（型安全なルート）。
 * `@Serializable` が付いた object を、NavHost の宛先として使う。
 *
 *     navController.navigate(HomeRoute)      // 画面を移動する
 *     composable<HomeRoute> { HomeScreen() } // その宛先で表示する画面を登録する
 *
 * 文字列だと、`"hom"` のような打ち間違いがコンパイルで見つからず、実行時に落ちる。
 * 型なら、打ち間違いはコンパイルエラーになる。
 *
 * 今回は、宛先が受け取る値（引数）が無いので `object`。
 * 引数がある宛先は `data class` にする（例：`data class UserDetailRoute(val id: Int)`）。
 */
@Serializable
object HomeRoute

@Serializable
object UsersRoute

@Serializable
object SettingsRoute

/**
 * バナーの詳細画面。どのバナーかを、`bannerId` という**引数**で受け取る。
 *
 * 引数がある宛先は、`object` ではなく `data class` にする。
 *
 *     navController.navigate(BannerDetailRoute(bannerId = 2))       // 渡す側
 *     val route = backStackEntry.toRoute<BannerDetailRoute>()       // 受け取る側
 *     route.bannerId                                                // → 2
 *
 * 文字列のルートで、"banner/2" のように書いて、あとで数字に直す必要が無い。
 * 型（Int）のまま、受け渡せる。
 */
@Serializable
data class BannerDetailRoute(val bannerId: Int)

/** 下部ナビゲーションには出さない、テーマの確認用の画面（Top App Bar のボタンから開く）。 */
@Serializable
object ThemeRoute
