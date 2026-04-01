/*
 *     Notes  Copyright (C) 2018  Nikhil Soni
 *     This program comes with ABSOLUTELY NO WARRANTY; for details type `show w'.
 *     This is free software, and you are welcome to redistribute it
 *     under certain conditions; type `show c' for details.
 *
 * The hypothetical commands `show w' and `show c' should show the appropriate
 * parts of the General Public License.  Of course, your program's commands
 * might be different; for a GUI interface, you would use an "about box".
 *
 *   You should also get your employer (if you work as a programmer) or school,
 * if any, to sign a "copyright disclaimer" for the program, if necessary.
 * For more information on this, and how to apply and follow the GNU GPL, see
 * <http://www.gnu.org/licenses/>.
 *
 *   The GNU General Public License does not permit incorporating your program
 * into proprietary programs.  If your program is a subroutine library, you
 * may consider it more useful to permit linking proprietary applications with
 * the library.  If this is what you want to do, use the GNU Lesser General
 * Public License instead of this License.  But first, please read
 * <http://www.gnu.org/philosophy/why-not-lgpl.html>.
 */

package com.nrs.nsnik.notes.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.nrs.nsnik.notes.model.CheckListObject
import com.nrs.nsnik.notes.util.DateSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@Entity
open class NoteEntity {

    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    var title: String? = null

    @Ignore
    var noteContent: String? = null
    var fileName: String? = null
    var folderName: String? = null
    var pinned: Int = 0
    var locked: Int = 0

    @TypeConverters(DateConverter::class)
    @Serializable(with = DateSerializer::class)
    var dateModified: Date? = null

    var color: String? = null

    @Ignore
    var imageList: List<String>? = null

    @Ignore
    var audioList: List<String>? = null

    @Ignore
    var checkList: List<CheckListObject>? = null

    @Ignore
    var hasReminder: Int = 0
}
