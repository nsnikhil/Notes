/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
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