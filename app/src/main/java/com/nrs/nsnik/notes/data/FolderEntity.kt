/*
 *     Credit Card Security V1  Copyright (C) 2018  sid-sun
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
import com.twitter.serial.serializer.ObjectSerializer
import com.twitter.serial.serializer.SerializationContext
import com.twitter.serial.stream.SerializerInput
import com.twitter.serial.stream.SerializerOutput

@Entity
class FolderEntity {

    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    var folderName: String? = null
    var parentFolderName: String? = null
    var pinned: Int = 0
    var locked: Int = 0
    var color: String? = null

    companion object {

        @Ignore
        val SERIALIZER: ObjectSerializer<FolderEntity> = FolderEntitySerializer()

        class FolderEntitySerializer : ObjectSerializer<FolderEntity>() {

            override fun serializeObject(context: SerializationContext, output: SerializerOutput<out SerializerOutput<*>>, folderEntity: FolderEntity) {
                output.writeInt(folderEntity.uid)
                output.writeString(folderEntity.folderName)
                output.writeString(folderEntity.parentFolderName)
                output.writeInt(folderEntity.pinned)
                output.writeInt(folderEntity.locked)
                output.writeString(folderEntity.color)
            }

            override fun deserializeObject(context: SerializationContext, input: SerializerInput, versionNumber: Int): FolderEntity? {
                val folderEntity = FolderEntity()
                folderEntity.uid = input.readInt()
                folderEntity.folderName = input.readString()
                folderEntity.parentFolderName = input.readString()
                folderEntity.pinned = input.readInt()
                folderEntity.locked = input.readInt()
                folderEntity.color = input.readString()
                return folderEntity
            }

        }

    }
}