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

#include <assert.h>
#include <stdlib.h>

#include "aterm1.h"
#include "byteio.h"
#include "util.h"

/*{{{  static void resize_buffer(byte_writer *writer, int delta) */

static void resize_buffer(byte_writer *writer, int delta)
{
  int size_needed, new_size;

  assert(writer->type == STRING_WRITER);

  size_needed = writer->u.string_data.cur_size + delta;
  if (size_needed >= writer->u.string_data.max_size) {
    new_size = MAX(size_needed, writer->u.string_data.max_size*2);
    writer->u.string_data.buf = realloc(writer->u.string_data.buf, new_size);
    if (!writer->u.string_data.buf) {
      ATerror("bafio: unable to resize buffer to %d bytes.\n", new_size);
    }
    writer->u.string_data.max_size = new_size;
  }
}

/*}}}  */

/*{{{  int write_byte(int byte, byte_writer *writer) */

int write_byte(int byte, byte_writer *writer)
{
  switch (writer->type) {
    case STRING_WRITER:
      resize_buffer(writer, 1);
      writer->u.string_data.buf[writer->u.string_data.cur_size++] = (char)byte;
      return byte;

    case FILE_WRITER:
      return fputc(byte, writer->u.file_data);
      
    default:
      abort();
  }
  return EOF;
}

/*}}}  */
/*{{{  int write_bytes(const char *buf, int count, byte_writer *writer) */

int write_bytes(const char *buf, int count, byte_writer *writer)
{
  switch (writer->type) {
    case STRING_WRITER:
      resize_buffer(writer, count);
      memcpy(&writer->u.string_data.buf[writer->u.string_data.cur_size], buf, count);
      writer->u.string_data.cur_size += count;
      return count;

    case FILE_WRITER:
      return fwrite(buf, 1, count, writer->u.file_data);
      
    default:
      abort();
  }
  return EOF;
}

/*}}}  */
/*{{{  int read_byte(byte_reader *reader) */

int read_byte(byte_reader *reader)
{
  int index, c;

  c = EOF;
  switch (reader->type) {
    case STRING_READER:
      index = reader->u.string_data.index;
      if (index >= reader->u.string_data.size) {
	return EOF;
      }
      reader->bytes_read++;
      reader->u.string_data.index++;
      c = ((int)reader->u.string_data.buf[index]) & 0xFF;
      break;

    case FILE_READER:
      c = fgetc(reader->u.file_data);
      reader->bytes_read++;
      break;
      
    default:
      abort();
  }

  return c;
}

/*}}}  */
/*{{{  int read_bytes(char *buf, int count, byte_reader *reader) */

int read_bytes(char *buf, int count, byte_reader *reader)
{
  int index, size, left, result;

  result = EOF;
  switch (reader->type) {
    case STRING_READER:
      index = reader->u.string_data.index;
      size  = reader->u.string_data.size;
      left  = size-index;
      if (left <= 0) {
	return EOF;
      }
      if (left < count) {
	count = left;
      }
      memcpy(buf, &reader->u.string_data.buf[index], count);
      reader->u.string_data.index += count;
      reader->bytes_read += count;
      result = count;
      break;

    case FILE_READER:
      result = fread(buf, 1, count, reader->u.file_data);
      reader->bytes_read += count;
      break;
      
    default:
      abort();
  }
  return result;
}

/*}}}  */
/*{{{  void init_file_reader(byte_reader *reader, FILE *file) */

void init_file_reader(byte_reader *reader, FILE *file)
{
  reader->type = FILE_READER;
  reader->bytes_read = 0;
  reader->u.file_data = file;
}

/*}}}  */
/*{{{  void init_string_reader(byte_reader *reader, char *buf, int max_size) */

void init_string_reader(byte_reader *reader, char *buf, int max_size)
{
  reader->type = STRING_READER;
  reader->bytes_read = 0;
  reader->u.string_data.buf = buf;
  reader->u.string_data.index = 0;
  reader->u.string_data.size = max_size;
}

/*}}}  */

