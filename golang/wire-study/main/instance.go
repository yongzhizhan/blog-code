package main

import "database/sql"

type Instance struct {
	Foo *Foo
	Bar *Bar
	Baz *Baz
}

type BOB struct {
	db *sql.DB
}

func NewBOB(db *sql.DB) *BOB {
	return &BOB{
		db: db,
	}
}

func main() {
	db := new(MysqlDB)
	NewBOB(db)
}
