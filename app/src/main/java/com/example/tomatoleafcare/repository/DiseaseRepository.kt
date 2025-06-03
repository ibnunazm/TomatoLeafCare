package com.example.tomatoleafcare.repository

import com.example.tomatoleafcare.R
import com.example.tomatoleafcare.model.Disease

object DiseaseRepository {
    val classList = listOf(
        Disease(
            "Bercak Daun Septoria",
            "Menyebabkan bercak coklat pada daun, pencegahan dengan fungisida dan sirkulasi udara yang baik.",
            "Jamur Septoria lycopersici, jamur ini sering ditemukan di tanah yang basah dan menyebar melalui percikan air.",
            "Bercak-bercak kecil berwarna coklat tua atau hitam pada daun yang dimulai dari bagian bawah dan dapat membesar.",
            "Daun menguning dan rontok., mengurangi produktivitas buah, dan membuat tanaman rentan terhadap penyakit lain.",
            "Penggunaan fungisida, memastikan sirkulasi udara yang baik, dan menghindari penyiraman berlebihan.",
            R.drawable.bercakdaunseptoria
        ),
        Disease(
            "Virus Mozaik",
            "Menyebabkan daun terdistorsi dan mosaik, cegah dengan benih sehat dan menghapus tanaman terinfeksi.",
            "Tomato Mosaic Virus (ToMV) yang menyebar melalui kontak tanaman terinfeksi, alat pertanian, dan serangga.",
            "Pola mosaik bercak hijau muda dan gelap pada daun, daun menjadi keriput atau terdistorsi",
            "Menghambat fotosintesis, menyebabkan pertumbuhan yang tidak optimal, serta menurunkan kualitas dan kuantitas buah.",
            "Menggunakan benih sehat, menjaga kebersihan alat, dan menghilangkan tanaman yang terinfeksi.",
            R.drawable.virusmozaik
        ),
        Disease(
            "Jamur Daun",
            "Menyebabkan bercak kuning dan lapisan beludru di bawah daun, cegah dengan kelembapan rendah dan fungisida.",
            "Jamur Cladosporium fulvum yang berkembang di lingkungan lembap dan sirkulasi udara buruk.",
            "Bercak kuning pada bagian atas daun dan lapisan beludru hijau atau coklat pada bagian bawah daun",
            "Mengurangi kemampuan fotosintesis, dapat menyebabkan daun mengering dan rontok.",
            "Menjaga kelembapan rendah, meningkatkan sirkulasi udara, menghindari penyiraman langsung pada daun, dan menggunakan fungisida.",
            R.drawable.jamurdaun
        ),
        Disease(
            "Virus Keriting Daun",
            "Menyebabkan daun keriting dan menguning, cegah dengan insektisida dan penghapusan tanaman terinfeksi.",
            "Virus yang ditularkan oleh kutu kebul (Bemisia tabaci).",
            "Daun menjadi keriting, tebal, dan berubah warna menjadi kuning.",
            "Menghambat pertumbuhan tanaman, menurunkan kualitas dan kuantitas buah.",
            "Menggunakan insektisida, dan segera hapus tanaman yang terinfeksi.",
            R.drawable.viruskeriting
        ),
        Disease(
            "Hawar Daun",
            "Menyebabkan bercak hitam, pembusukan buah, cegah dengan fungisida dan rotasi tanaman.",
            "Jamur Phytophthora infestans, yang menyebar cepat dalam kondisi lembap dan dingin.",
            "Bercak coklat gelap atau hitam menyebar cepat pada daun, dengan lapisan putih pada bagian bawah daun.",
            "Bisa dengan cepat menghancurkan seluruh tanaman jika tidak segera diatasi, buah juga bisa membusuk.",
            "Gunakan fungisida terutama saat musim hujan, dan lakukan rotasi tanaman untuk mencegah infeksi ulang",
            R.drawable.lateblight
        ),
        Disease(
            "Bercak Bakteri",
            "Menyebabkan bercak hitam pada daun dan buah, cegah dengan benih sehat dan sanitasi.",
            "Bakteri Xanthomonas campestris pv. vesicatoria, yang menyebar melalui air, angin, dan alat pertanian.",
            "Bercak hitam kecil muncul pada daun, batang, dan buah.",
            "Merusak jaringan daun, Menghambat fotositesis dan Menurunkan kualitas buah,",
            "Gunakan benih sehat, jaga sanitasi, dan gunakan bakterisida.",
            R.drawable.bercakbakteri
        ),
        Disease(
            "Tanaman Sehat",
            "Tidak ditemukan penyakit pada daun ini.",
            "tidak ada",
            "tidak ada",
            "tidak ada",
            "tidak ada",
            R.drawable.sehat
        )

    )

    fun getData(): List<Disease> {
        return classList
    }

    fun findDiseaseByName(name: String): Disease? {
        return classList.find { it.name.equals(name, ignoreCase = true) }
    }

}

object DiseaseMatcher {
    fun matchDisease(className: String): Disease? {
        return when (className) {
            "bacterial_spot" -> DiseaseRepository.classList.find { it.name.contains("Bercak Bakteri", ignoreCase = true) }
            "healthy" -> DiseaseRepository.classList.find { it.name.contains("Tanaman Sehat", ignoreCase = true) }
            "late_blight" -> DiseaseRepository.classList.find { it.name.contains("Hawar Daun", ignoreCase = true) }
            "leaf_curl_virus" -> DiseaseRepository.classList.find { it.name.contains("Virus Keriting Daun", ignoreCase = true) }
            "leaf_mold" -> DiseaseRepository.classList.find { it.name.contains("Jamur Daun", ignoreCase = true) }
            "mosaic_virus" -> DiseaseRepository.classList.find { it.name.contains("Virus Mozaik", ignoreCase = true) }
            "septoria_leaf_spot" -> DiseaseRepository.classList.find { it.name.contains("Bercak Daun Septoria", ignoreCase = true) }
            else -> null
        }
    }
}


