/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.model


import com.twitter.serial.serializer.ObjectSerializer
import com.twitter.serial.serializer.SerializationContext
import com.twitter.serial.stream.SerializerInput
import com.twitter.serial.stream.SerializerOutput
import java.io.Serializable


class CheckListObject : Serializable {

    var text: String = ""
    var done: Boolean = false

    companion object {

        val SERIALIZER: ObjectSerializer<CheckListObject> = CheckListObjectSerializer()

        class CheckListObjectSerializer : ObjectSerializer<CheckListObject>() {

            override fun serializeObject(context: SerializationContext, output: SerializerOutput<out SerializerOutput<*>>, checkListObject: CheckListObject) {
                output.writeString(checkListObject.text)
                output.writeBoolean(checkListObject.done)
            }

            override fun deserializeObject(context: SerializationContext, input: SerializerInput, versionNumber: Int): CheckListObject? {
                val checkListObject = CheckListObject()
                checkListObject.text = input.readString()!!
                checkListObject.done = input.readBoolean()
                return checkListObject
            }
        }
    }
}
