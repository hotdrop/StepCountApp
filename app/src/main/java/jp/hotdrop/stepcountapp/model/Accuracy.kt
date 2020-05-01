package jp.hotdrop.stepcountapp.model

sealed class Accuracy {
    object High: Accuracy()
    object Medium: Accuracy()
    object Low: Accuracy()
    object Unreliable: Accuracy()
    object NoContact: Accuracy()
}