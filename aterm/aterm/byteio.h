/*

    ATerm -- The ATerm (Annotated Term) library
    Copyright (C) 1998-2000  Stichting Mathematisch Centrum, Amsterdam, 
                             The  Netherlands.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA

*/

#ifndef BYTEIO_H
#define BYTEIO_H

#include <stdio.h>

#define FILE_WRITER   0
#define STRING_WRITER 1

#define FILE_READER   0
#define STRING_READER 1

typedef struct
{
  int type;
  union {
    FILE *file_data;
    struct {
      char *buf;
      int   max_size;
      int   cur_size;
    } string_data;
  } u;
} byte_writer;

typedef struct
{
  int type;
  long bytes_read;
  union {
    FILE *file_data;
    struct {
      char *buf;
      int   index;
      int   size;
    } string_data;
  } u;
} byte_reader;

int write_byte(int byte, byte_writer *writer);
int write_bytes(const char *buf, int count, byte_writer *writer);
int read_byte(byte_reader *reader);
int read_bytes(char *buf, int count, byte_reader *reader);
void init_file_reader(byte_reader *reader, FILE *file);
void init_string_reader(byte_reader *reader, char *buf, int max_size);

#endif
