package main

import "fmt"

type Baz struct {
	foo *Foo
	bar *Bar
}

func NewBaz(foo *Foo, bar *Bar) *Baz {
	return &Baz{
		foo: foo,
		bar: bar,
	}
}

func (p *Baz) Test() {
	fmt.Println("hello")
}
