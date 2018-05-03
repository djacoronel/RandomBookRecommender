package com.djacoronel.randombookrecommender


class Book {
    var id: String? = null
    var selfLink: String? = null
    var volumeInfo: VolumeInfo = VolumeInfo()

}

class VolumeInfo {
    var title: String? = null
    var subtitle: String? = null
    var authors: List<String>? = null
    var publisher: String? = null
    var publishdDate: String? = null
    var description: String? = null
    var imageLinks: ImageLinks = ImageLinks()
}

class ImageLinks{
    var thumbnail: String? = null
}
