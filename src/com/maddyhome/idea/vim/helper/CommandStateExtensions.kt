/*
 * IdeaVim - Vim emulator for IDEs based on the IntelliJ platform
 * Copyright (C) 2003-2020 The IdeaVim authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

@file:JvmName("CommandStateHelper")

package com.maddyhome.idea.vim.helper

import com.intellij.openapi.editor.Editor
import com.maddyhome.idea.vim.action.motion.leftright.MotionLastColumnAction
import com.maddyhome.idea.vim.action.motion.updown.*
import com.maddyhome.idea.vim.command.Command
import com.maddyhome.idea.vim.command.CommandState
import com.maddyhome.idea.vim.option.OptionsManager

val usesVirtualSpace
  get() = OptionsManager.virtualedit.value == "onemore"

val CommandState.Mode.isEndAllowed : Boolean
  get() = when (this) {
    CommandState.Mode.INSERT, CommandState.Mode.VISUAL, CommandState.Mode.SELECT -> true
    CommandState.Mode.COMMAND, CommandState.Mode.CMD_LINE, CommandState.Mode.REPLACE, CommandState.Mode.OP_PENDING -> usesVirtualSpace
  }

val CommandState.Mode.isEndAllowedIgnoringOnemore : Boolean
  get() = when (this) {
    CommandState.Mode.INSERT, CommandState.Mode.VISUAL, CommandState.Mode.SELECT -> true
    CommandState.Mode.COMMAND, CommandState.Mode.CMD_LINE, CommandState.Mode.REPLACE, CommandState.Mode.OP_PENDING -> false
  }

val CommandState.isEndAllowed:Boolean
  get() {
    val isAllowedByMode = this.mode.isEndAllowed
    val trimCase = (this.mode == CommandState.Mode.COMMAND) && (this.previousCommand?.action is MotionLastColumnAction)

    return isAllowedByMode && !trimCase
  }

val CommandState.Mode.isBlockCaret
  get() = when (this) {
    CommandState.Mode.VISUAL, CommandState.Mode.COMMAND, CommandState.Mode.OP_PENDING -> true
    CommandState.Mode.INSERT, CommandState.Mode.CMD_LINE, CommandState.Mode.REPLACE, CommandState.Mode.SELECT -> false
  }

val CommandState.Mode.hasVisualSelection
  get() = when (this) {
    CommandState.Mode.VISUAL, CommandState.Mode.SELECT -> true
    CommandState.Mode.REPLACE, CommandState.Mode.CMD_LINE, CommandState.Mode.COMMAND, CommandState.Mode.INSERT, CommandState.Mode.OP_PENDING -> false
  }

val Editor.mode
  get() = this.commandState.mode

var Editor.subMode
  get() = this.commandState.subMode
  set(value) {
    this.commandState.subMode = value
  }

var Editor.previousCommand: Command?
  get() = this.commandState.previousCommand
  set(value) {
    this.commandState.previousCommand = value
  }

val Command.isUpDownMotion: Boolean
  get() = (
         this.action is MotionDownBase
      || this.action is MotionUpBase
      || this.action is MotionUpAction
      || this.action is MotionDownAction
      || this.action is MotionArrowUpAction
      || this.action is MotionArrowDownAction)


@get:JvmName("inNormalMode")
val Editor.inNormalMode
  get() = this.mode == CommandState.Mode.COMMAND

@get:JvmName("inInsertMode")
val Editor.inInsertMode
  get() = this.mode == CommandState.Mode.INSERT || this.mode == CommandState.Mode.REPLACE

@get:JvmName("inRepeatMode")
val Editor.inRepeatMode
  get() = this.commandState.isDotRepeatInProgress

@get:JvmName("inVisualMode")
val Editor.inVisualMode
  get() = this.mode == CommandState.Mode.VISUAL

@get:JvmName("inSelectMode")
val Editor.inSelectMode
  get() = this.mode == CommandState.Mode.SELECT

@get:JvmName("inBlockSubMode")
val Editor.inBlockSubMode
  get() = this.subMode == CommandState.SubMode.VISUAL_BLOCK

@get:JvmName("inSingleCommandMode")
val Editor.inSingleCommandMode
  get() = this.subMode == CommandState.SubMode.SINGLE_COMMAND && this.inNormalMode

@get:JvmName("commandState")
val Editor?.commandState
  get() = CommandState.getInstance(this)
